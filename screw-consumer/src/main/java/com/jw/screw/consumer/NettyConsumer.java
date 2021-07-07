package com.jw.screw.consumer;

import com.jw.screw.common.ConnectionCallback;
import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.NotifyListener;
import com.jw.screw.common.event.Observer;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.future.InvokeFuture;
import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.transport.body.RequestBody;
import com.jw.screw.common.util.Collections;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.consumer.invoker.RpcInvoker;
import com.jw.screw.consumer.process.NettyConsumerBroadcastProcessor;
import com.jw.screw.consumer.process.NettyConsumerSubscribeProcessor;
import com.jw.screw.loadbalance.BaseLoadBalancer;
import com.jw.screw.loadbalance.LoadBalancer;
import com.jw.screw.monitor.api.ScrewMonitor;
import com.jw.screw.monitor.opentracing.TracerCache;
import com.jw.screw.remote.Invoker;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.netty.ChannelGroup;
import com.jw.screw.remote.netty.NettyChannelGroup;
import com.jw.screw.remote.netty.SConnector;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jiangw
 */
public class NettyConsumer extends com.jw.screw.consumer.AbstractConsumer {

    private static Logger logger = LoggerFactory.getLogger(NettyConsumer.class);

    private final com.jw.screw.consumer.NettyConsumerConfig consumerConfig;

    private ScrewMonitor monitor;

    public NettyConsumer() {
        this(new com.jw.screw.consumer.NettyConsumerConfig());
    }

    public NettyConsumer(com.jw.screw.consumer.NettyConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
        NettyClientConfig rpcConfig = consumerConfig.getRpcConfig();
        if (rpcConfig == null) {
            rpcConfig = new NettyClientConfig();
            consumerConfig.setRpcConfig(rpcConfig);
        }
        this.rpcClient = new com.jw.screw.consumer.RpcClient(consumerConfig);
        NettyClientConfig registryConfig = consumerConfig.getRegistryConfig();
        if (registryConfig != null) {
            register(registryConfig.getDefaultAddress().getHost(), registryConfig.getDefaultAddress().getPort());
        }
    }

    @Override
    public com.jw.screw.consumer.ConnectionWatcher watchConnect(final ServiceMetadata serviceMetadata) throws InterruptedException, ConnectionException {
        com.jw.screw.consumer.ConnectionWatcher connectionWatcher = new com.jw.screw.consumer.ConnectionWatcher() {

            final ReentrantLock notifyLock = new ReentrantLock();

            final Condition notify = notifyLock.newCondition();

            /**
             * 如果为true，表示需要通知那些被阻塞的线程
             * 如果没有可能存在的情况：
             * 线程A等待连接，线程B完成连接，执行onSuccess()那么那些被阻塞等待连接的将会被激活。
             */
            private final AtomicBoolean isSignal = new AtomicBoolean(true);

            private final AtomicBoolean isActive = new AtomicBoolean(false);

            /**
             * 注册中心返回服务集群。只有所有的服务可以访问后才能继续调用rpc
             */
            ChannelGroup[] channelGroups;

            @Override
            public void start() throws ConnectionException, InterruptedException {
                subscribe(serviceMetadata, new NotifyListener() {
                    @Override
                    public void notify(NotifyEvent event, RegisterMetadata... registerInfos) {
                        if (registerInfos != null) {
                            try {
                                channelGroups = new ChannelGroup[registerInfos.length];
                                for (int i = 0; i < registerInfos.length; i++) {
                                    RegisterMetadata registerInfo = registerInfos[i];
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("subscribe service info {}", registerInfo);
                                    }
                                    UnresolvedAddress providerAddress = registerInfo.getUnresolvedAddress();
                                    if (event == NotifyEvent.ADD) {
                                        SConnector connector = connector(serviceMetadata, providerAddress, registerInfo.getWeight());
                                        ChannelGroup channelGroup = connector.channelGroup();
                                        channelGroups[i] = channelGroup;
                                        connector.setReConnect(false);
                                        // 通道已经与地址绑定
                                        if (!channelGroup.isAvailable()) {
                                            int connCount = registerInfo.getConnCount();
                                            for (int count = 0; count < connCount; count++) {
                                                Channel channel = connector.createChannel();
                                                channelGroup.add(channel);
                                            }
                                        }
                                    } else if (event == NotifyEvent.REMOVED) {
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("cancel subscribe service info {}", registerInfo);
                                        }
                                        // 移除缓存数据
                                        ServiceMetadata serviceMetadata = new ServiceMetadata(registerInfo.getServiceProviderName());
                                        ConcurrentHashMap<UnresolvedAddress, SConnector> serviceGroups = subscribeAddress.get(serviceMetadata);
                                        if (Collections.isEmpty(serviceGroups)) {
                                            subscribeAddress.remove(serviceMetadata);
                                        } else {
                                            SConnector sConnector = serviceGroups.get(providerAddress);
                                            if (sConnector != null) {
                                                // 关闭连接器
                                                sConnector.close();
                                            }
                                            serviceGroups.remove(providerAddress);
                                            // 标识当前watcher channel group = null
                                            channelGroups = null;
                                        }
                                    }
                                }
                            } catch (InterruptedException | ConnectionException | ExecutionException e) {
                                isSignal.set(false);
                                e.printStackTrace();
                            }
                        }
                        onSuccess(isSignal.getAndSet(false));
                    }
                });
            }

            /**
             * 通知那些等待连接可用的线程
             */
            private void onSuccess(boolean isSignal) {
                if (isSignal) {
                    notifyLock.lock();
                    try {
                        isActive.set(true);
                        notify.signalAll();
                    } finally {
                        notifyLock.unlock();
                    }
                }
            }

            @Override
            public boolean waitForAvailable(long millis) throws InterruptedException {
                long waitNanos = TimeUnit.MILLISECONDS.toNanos(millis);
                isSignal.set(true);
                notifyLock.lock();
                try {
                    while (channelGroups == null || channelGroups.length == 0) {
                        if ((waitNanos = notify.awaitNanos(waitNanos)) <= 0) {
                            return false;
                        }
                    }
                    for (ChannelGroup channelGroup : channelGroups) {
                        if (channelGroup.isAvailable()) {
                            return true;
                        }
                    }
                    while (!isActive.get()) {
                        if ((waitNanos = notify.awaitNanos(waitNanos)) <= 0) {
                            return false;
                        }
                    }
                } finally {
                    notifyLock.unlock();
                }
                return true;
            }
        };
        subscribeService.add(serviceMetadata);
        connectionWatcher.start();
        return connectionWatcher;
    }

    public void register(String host, int port) {
        register(new RemoteAddress(host, port));
    }

    public void register(UnresolvedAddress unresolvedAddress) {
        NettyClientConfig registryConfig = new NettyClientConfig();
        registryConfig.setDefaultAddress(unresolvedAddress);
        consumerConfig.setRegistryConfig(registryConfig);
        this.registerClient = new com.jw.screw.consumer.ConsumerClient(consumerConfig, this);
        this.registerClient.registerProcessors(Protocol.Code.RESPONSE_SUBSCRIBE,
                new NettyConsumerSubscribeProcessor(notifyListeners),
                new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("consumer subscribe")));
        this.registerClient.registerProcessors(Protocol.Code.BROADCAST,
                new NettyConsumerBroadcastProcessor(),
                new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("consumer broadcast")));
    }

    public void directService(ServiceMetadata serviceMetadata, String host, int port) throws InterruptedException, ConnectionException, ExecutionException {
        directService(serviceMetadata, new RemoteAddress(host, port));
    }

    public void directService(ServiceMetadata serviceMetadata, UnresolvedAddress socketAddress) throws InterruptedException, ConnectionException, ExecutionException {
        SConnector connector = connector(serviceMetadata, socketAddress);
        Channel channel = connector.createChannel();
        connector.channelGroup().add(channel);
    }

    public com.jw.screw.consumer.NettyConsumerConfig getConfig() {
        return this.consumerConfig;
    }

    @Override
    public void start(Observer observer) throws InterruptedException, ConnectionException, ExecutionException {
        if (logger.isDebugEnabled()) {
            logger.info("started rpc client");
        }
        rpcClient.start();
        if (registerClient != null) {
            if (logger.isDebugEnabled()) {
                logger.info("started register client");
            }
            registerClient.start();
            registerClient.connect(new NettyChannelGroup(), new ConnectionCallback(observer) {
                @Override
                public void acceptable(Object accept) throws ConnectionException {
                    super.acceptable(accept);
                    if (accept != null) {
                        registerConnector = (SConnector) accept;
                    }
                }
            });
        }
        registerConnector.setReConnect(true);
        if (StringUtils.isNotEmpty(consumerConfig.getMonitorServerKey())) {
            monitor = new ScrewMonitor(registerConnector, consumerConfig);
            registerClient.setMonitor(monitor, consumerConfig.getMonitorServerKey());
            rpcClient.setMonitor(monitor, consumerConfig.getMonitorServerKey());
        }
    }

    @Override
    public void stop() throws InterruptedException {
        // 关闭与rpc的连接器
        if (Collections.isNotEmpty(subscribeAddress)) {
            for (Map.Entry<ServiceMetadata, ConcurrentHashMap<UnresolvedAddress, SConnector>> addressEntry : subscribeAddress.entrySet()) {
                ConcurrentHashMap<UnresolvedAddress, SConnector> connectors = addressEntry.getValue();
                if (Collections.isNotEmpty(connectors)) {
                    for (Map.Entry<UnresolvedAddress, SConnector> connector : connectors.entrySet()) {
                        connector.getValue().close();
                    }
                }
            }
        }
        rpcClient.shutdownGracefully();
        if (registerClient != null) {
            registerConnector.close();
            registerClient.shutdownGracefully();
        }
        if (monitor != null) {
            monitor.shutdown();
        }
    }

    @Override
    public Object call(ServiceMetadata serviceMetadata, String serviceName, String methodName, Class<?> returnType, Object[] args) {
        return callContext(serviceMetadata, serviceName, methodName, args, returnType, false);
    }

    @Override
    public InvokeFuture<?> asyncCall(ServiceMetadata serviceMetadata, String serviceName, String methodName, Class<?> returnType, Object[] args) {
        Object result = callContext(serviceMetadata, serviceName, methodName, args, returnType, true);
        if (result == null) {
            return null;
        }
        if (result instanceof InvokeFuture<?>) {
            return (InvokeFuture<?>) result;
        }
        return null;
    }

    private Object callContext(ServiceMetadata serviceMetadata, String serviceName, String methodName, Object[] args, Class<?> returnType, boolean async) {
        ConcurrentHashMap<UnresolvedAddress, SConnector> serviceGroups = subscribeAddress.get(serviceMetadata);
        if (Collections.isEmpty(serviceGroups)) {
            throw new IllegalArgumentException("service provider: " + serviceMetadata.getServiceProviderName() + " can't find");
        }
        // 负载均衡
        LoadBalancer loadBalancer = new BaseLoadBalancer(serviceGroups);
        loadBalancer.setRule(consumerConfig.getRule());
        SConnector connector = loadBalancer.selectServer();
        if (connector == null) {
            throw new IllegalArgumentException("load balance error, maybe service isn't useful.");
        }
        Invoker invoker = new RpcInvoker(connector.channelGroup(), async);
        try {
            RequestBody requestBody = new RequestBody();
            requestBody.setServiceName(serviceName);
            requestBody.setMethodName(methodName);
            requestBody.setParameters(args);
            requestBody.setExpectedReturnType(returnType);
            // 获取缓存中tracer
            requestBody.attachment(TracerCache.take());
            return invoker.invoke(requestBody, rpcClient);
        } catch (InterruptedException | RemoteTimeoutException | RemoteSendException e) {
            logger.error("invoke rpc errors: {}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
