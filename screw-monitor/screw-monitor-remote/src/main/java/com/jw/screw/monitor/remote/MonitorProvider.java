package com.jw.screw.monitor.remote;

import com.jw.screw.monitor.core.HealthStatus;
import com.jw.screw.monitor.core.MonitorModel;
import com.jw.screw.monitor.core.mircometer.Metrics;
import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.model.Tuple;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.util.Collections;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.monitor.opentracing.ScrewSpan;
import com.jw.screw.monitor.remote.processor.NettyMetricsProcessor;
import com.jw.screw.monitor.remote.processor.NettyTracingProcessor;
import com.jw.screw.provider.NettyProvider;
import com.jw.screw.provider.NettyProviderConfig;
import com.jw.screw.provider.ServiceWrapperManager;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.netty.config.NettyServerConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * remote monitor server
 * @author jiangw
 * @date 2020/12/22 9:01
 * @since 1.0
 */
public class MonitorProvider extends NettyProvider {

    /**
     * 是否收集性能指标
     */
    private final AtomicBoolean isCollect = new AtomicBoolean(false);

    /**
     * 某个服务的metrics。
     */
    private Tuple<MonitorModel, Map<String, List<Metrics>>> serverMetrics = null;

    /**
     * 服务的健康情况
     * key: {@link MonitorModel}
     * value: {@link HealthStatus}
     */
    private ConcurrentHashMap<MonitorModel, String> serverHealth = null;

    /**
     * key: serverKey&address
     * value: {@link TracingModel}
     */
    private ConcurrentHashMap<String, TracingModel> serverTracing = null;

    /**
     * 存储那些服务发送的span
     */
    private final List<ScrewSpan> clutterSpans = new CopyOnWriteArrayList<>();

    /**
     * 激活的Monitor，用于指标收集
     */
    private MonitorModel activeMonitor;

    private final ReentrantLock collectLock = new ReentrantLock();

    /**
     * 健康的判定的时间，单位是{@link TimeUnit#SECONDS}
     */
    private static final long HEALTH_LISTEN_TIME = 300;

    public MonitorProvider(NettyProviderConfig monitorConfig) {
        super(monitorConfig);

        /**
         * 服务健康状况的定时器
         */
        ScheduledExecutorService healthListener = Executors.newScheduledThreadPool(1, new NamedThreadFactory("server health listener", true));
        healthListener.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                collectLock.lock();
                try {
                    if (Collections.isNotEmpty(serverHealth)) {
                        Map<MonitorModel, String> deleted = new HashMap<>();
                        for (Map.Entry<MonitorModel, String> entry : serverHealth.entrySet()) {
                            MonitorModel key = entry.getKey();
                            boolean isDie = key.judgeDies(TimeUnit.SECONDS.toMillis(HEALTH_LISTEN_TIME));
                            if (isDie) {
                                deleted.put(key, entry.getValue());
                            }
                        }
                        if (Collections.isNotEmpty(deleted)) {
                            for (Map.Entry<MonitorModel, String> entry : deleted.entrySet()) {
                                serverHealth.put(entry.getKey(), HealthStatus.CLOSE);
                            }
                        }
                    }
                } finally {
                    collectLock.unlock();
                }
            }
        }, 0, HEALTH_LISTEN_TIME, TimeUnit.SECONDS);
    }

    @Override
    protected void initialize() {
        // 指定provider的netty服务为monitor
        NettyServerConfig rpcServerConfig = providerConfig.getRpcServerConfig();
        if (rpcServerConfig == null) {
            if (StringUtils.isEmpty(providerConfig.getServerHost())) {
                rpcServerConfig = new NettyServerConfig(providerConfig.getPort());
            } else {
                rpcServerConfig = new NettyServerConfig(providerConfig.getServerHost(), providerConfig.getPort());
            }
            providerConfig.setRpcServerConfig(rpcServerConfig);
        }
        serviceWrapperManager = new ServiceWrapperManager();
        rpcServer = new MonitorServer(rpcServerConfig);
        registerProcessor();
    }

    @Override
    public void registerProcessor() {
        super.registerProcessor();
        // 性能指标线程池
        rpcServer.registerProcessors(Protocol.Code.METRICS,
                new NettyMetricsProcessor(this),
                new ThreadPoolExecutor(0, 10, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new NamedThreadFactory("metrics exec", true)));
        // 链路追踪线程池
        rpcServer.registerProcessors(Protocol.Code.TRACING,
                new NettyTracingProcessor(this),
                new ThreadPoolExecutor(0, 10, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new NamedThreadFactory("tracing exec", true)));
    }

    /**
     * 收集服务的信息
     * @param monitorModel {@link MonitorModel}
     * @param metrics {@link Metrics}
     */
    public void collect(MonitorModel monitorModel, Map<String, List<Metrics>> metrics) {
        collectLock.lock();
        try {
            Map<MonitorModel, String> serverHealth = getServerHealth(null);
            // 如果某个服务第一次提供metrics，那么isExist为null，此时服务是健康的
            // 如果某个服务第二次提供metrics，那么isExist不为null，此时需要更新在Map中存放的MonitorModel的健康时间
            // 如果某个服务长时间没有提供metrics（在health_time < time < 2 * health_time 之间），那么它将被判定为死亡（只是在监控中心）。
            String isExist = serverHealth.put(monitorModel, HealthStatus.HEALTH);
            if (StringUtils.isNotEmpty(isExist)) {
                Set<Map.Entry<MonitorModel, String>> collect = serverHealth.entrySet().stream()
                        .filter(o -> o.getKey().equals(monitorModel))
                        .collect(Collectors.toSet());
                if (Collections.isNotEmpty(collect)) {
                    for (Map.Entry<MonitorModel, String> entry : collect) {
                        MonitorModel key = entry.getKey();
                        key.updateTime();
                    }
                }
            }
            // 在某一时刻只有一个server允许被收集指标信息，否则内存很容易撑爆
            if (isCollect.get() && activeMonitor != null) {
                if (activeMonitor.equals(monitorModel)) {
                    serverMetrics = new Tuple<>(monitorModel, metrics);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            collectLock.unlock();
        }
    }

    public Tuple<MonitorModel, Map<String, List<Metrics>>> getServerMetrics() {
        return this.serverMetrics;
    }

    /**
     * @see #getServerHealth(MonitorModel)
     */
    public Map<MonitorModel, String> getServerHealthByKey(String serverKey) {
        if (StringUtils.isEmpty(serverKey)) {
            return getServerHealth(null);
        } else {
            return getServerHealth(new MonitorModel(serverKey, null));
        }
    }

    /**
     * 获取某个服务的健康情况，有两条分支，一条由{{@link #getServerHealthByKey(String)}}使用，一条由{{@link #collect(MonitorModel, Map)}}使用
     * @param searchMonitor {@link MonitorModel}
     * @return
     */
    Map<MonitorModel, String> getServerHealth(MonitorModel searchMonitor) {
        if (Collections.isEmpty(serverHealth)) {
            serverHealth = new ConcurrentHashMap<>();
        }
        if (searchMonitor == null) {
            return serverHealth;
        }
        String serverKey = searchMonitor.getServerKey();
        if (StringUtils.isNotEmpty(serverKey)) {
            return serverHealth.entrySet().stream()
                    .filter(o -> o.getKey().getServerKey().contains(serverKey))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            return serverHealth;
        }
    }

    public ConcurrentHashMap<String, TracingModel> getServerTracing() {
        if (Collections.isEmpty(serverTracing)) {
            serverTracing = new ConcurrentHashMap<>();
        }
        return serverTracing;
    }

    public List<ScrewSpan> getClutterSpans() {
        return clutterSpans;
    }

    public void activeMetricsCollect(String serverKey, UnresolvedAddress serverAddress) {
        collectLock.lock();
        try {
            isCollect.set(true);
            this.activeMonitor = new MonitorModel(serverKey, serverAddress);
        } finally {
            collectLock.unlock();
        }
    }

    public void stopMetricsCollect() {
        collectLock.lock();
        try {
            isCollect.set(false);
            this.activeMonitor = null;
            this.serverMetrics = null;
        } finally {
            collectLock.unlock();
        }
    }
}
