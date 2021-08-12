package com.jw.screw.monitor.api;

import com.jw.screw.common.ConnectionCallback;
import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.transport.body.SubscribeBody;
import com.jw.screw.common.util.Collections;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.loadbalance.BaseConfig;
import com.jw.screw.loadbalance.BaseLoadBalancer;
import com.jw.screw.loadbalance.LoadBalancer;
import com.jw.screw.monitor.core.body.MetricsBody;
import com.jw.screw.monitor.core.mircometer.Metrics;
import com.jw.screw.monitor.core.mircometer.MetricsObjectFactory;
import com.jw.screw.remote.Protocol;
import com.jw.screw.common.util.Remotes;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.NettyChannelGroup;
import com.jw.screw.remote.netty.NettyClient;
import com.jw.screw.remote.netty.SConnector;
import com.jw.screw.remote.netty.config.GlobeConfig;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 1.创建一个单线程池等待获取注册中心
 * 2.channel是从provider、consumer中得到，所以当获取成功后，由aqs条件通知获取成功
 * 3.创建一个定时发送线程池，阻塞队列组成并且connector不为空
 * 4.性能指标、追踪消息不保证消息可靠性
 * @author jiangw
 * @date 2020/12/23 10:34
 * @since 1.0
 */
public class ScrewMonitor implements com.jw.screw.monitor.api.Monitor {

    private static Logger logger = LoggerFactory.getLogger(ScrewMonitor.class);

    private final BaseConfig config;

    /**
     * 注册元数据
     */
    private RegisterMetadata[] monitorMetadata;

    /**
     * 监控中心地址
     */
    private final Map<UnresolvedAddress, SConnector> monitorConnectors = new ConcurrentHashMap<>();

    /**
     * 当前角色作为监控中心的客户端
     */
    private NettyClient monitorClient;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 成功监控中心地址的获取条件
     */
    private final Condition fetch = lock.newCondition();

    private final SConnector registryConnector;

    /**
     * 定时发送性能指标线程池
     */
    private final ScheduledExecutorService periodMetrics = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("monitor client metrics", true));

    private final ThreadPoolExecutor fetchMonitor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new NamedThreadFactory("monitor fetch registry"));

    public ScrewMonitor(SConnector registryConnector, BaseConfig config) throws ConnectionException {
        this.config = config;
        this.registryConnector = registryConnector;
        try {
            monitorClient = new NettyClient(new NettyClientConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String monitorServiceKey = config.getMonitorServerKey();
        if (StringUtils.isNotEmpty(monitorServiceKey)) {
            connectionAddress();
            retry();
            periodMetrics.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    send();
                }
            }, 0, config.getMonitorCollectPeriod(), config.getMonitorCollectUnit());
        }
    }

    @Override
    public void send() {
        lock.lock();
        try {
            if (Collections.isNotEmpty(monitorConnectors)) {
                Map<String, List<Metrics>> metrics = MetricsObjectFactory.getMetrics();
                MetricsBody metricsBody = new MetricsBody();
                metricsBody.setMetricsMap(metrics);
                metricsBody.setServerKey(config.getServerKey());
                metricsBody.setAddress(new RemoteAddress(Remotes.getHost(), config.getPort()));
                metricsBody.setRole(config.getRole());
                // 负载均衡
                SConnector connector = getConnector();
                if (connector != null) {
                    Channel channel = connector.createChannel();
                    RemoteTransporter transporter = RemoteTransporter.createRemoteTransporter(Protocol.Code.METRICS, metricsBody);
                    channel.writeAndFlush(transporter);
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("monitor load balance error, maybe service isn't useful.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public SConnector getConnector() {
        LoadBalancer loadBalancer = new BaseLoadBalancer(monitorConnectors);
        loadBalancer.setRule(config.getRule());
        return loadBalancer.selectServer();
    }

    public void retry() {
        monitorConnectors.clear();
        fetchMonitor.submit(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                try {
                    while (monitorMetadata == null || monitorMetadata.length == 0) {
                        if (fetch.awaitNanos(TimeUnit.MILLISECONDS.toNanos(GlobeConfig.CONNECT_TIMEOUT_MILLIS)) > 0) {
                            break;
                        }
                    }
                    for (RegisterMetadata metadata : monitorMetadata) {
                        // 获取监控中心地址成功，连接监控中心
                        UnresolvedAddress monitorAddress = metadata.getUnresolvedAddress();
                        NettyClientConfig monitorConfig = new NettyClientConfig();
                        monitorClient = new NettyClient(monitorConfig);
                        monitorClient.connect(monitorAddress, new NettyChannelGroup(), metadata.getWeight(), new ConnectionCallback(null) {
                            @Override
                            public void acceptable(Object accept) throws ConnectionException {
                                super.acceptable(accept);
                                if (accept != null) {
                                    SConnector connector = (SConnector) accept;
                                    connector.setReConnect(true);
                                    monitorConnectors.put(monitorAddress, connector);
                                }
                            }
                        });
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    public void setMonitorAddress(RegisterMetadata... monitorMetadata) {
        lock.lock();
        try {
            this.monitorMetadata = monitorMetadata;
            fetch.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() throws InterruptedException {
        if (monitorClient != null) {
            monitorClient.shutdownGracefully();
        }
        for (Map.Entry<UnresolvedAddress, SConnector> entry : monitorConnectors.entrySet()) {
            SConnector connector = entry.getValue();
            if (connector != null) {
                connector.close();
            }
        }
        periodMetrics.shutdown();
    }

    public void connectionAddress() {
        String monitorServiceKey = config.getMonitorServerKey();
        if (StringUtils.isNotEmpty(monitorServiceKey)) {
            try {
                ServiceMetadata serviceMetadata = new ServiceMetadata(monitorServiceKey);
                // 获取监控中心的地址
                Channel channel = registryConnector.createChannel();
                SubscribeBody subscribeBody = new SubscribeBody();
                subscribeBody.setServiceMetadata(serviceMetadata);
                RemoteTransporter remoteTransporter = RemoteTransporter.createRemoteTransporter(Protocol.Code.MONITOR_ADDRESS, subscribeBody);
                channel.writeAndFlush(remoteTransporter);
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }
    }
}
