package com.jw.screw.consumer;

import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.NotifyListener;
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
import com.jw.screw.consumer.filter.*;
import com.jw.screw.consumer.invoker.Invoker;
import com.jw.screw.consumer.invoker.RpcInvoker;
import com.jw.screw.consumer.process.NettyConsumerBroadcastProcessor;
import com.jw.screw.consumer.process.NettyConsumerSubscribeProcessor;
import com.jw.screw.loadbalance.BaseLoadBalancer;
import com.jw.screw.loadbalance.LoadBalancer;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.netty.ChannelGroup;
import com.jw.screw.remote.netty.NettyChannelGroup;
import com.jw.screw.remote.netty.NettyClient;
import com.jw.screw.remote.netty.SConnector;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jiangw
 */
public class NettyConsumer extends AbstractConsumer {

    private static Logger logger = LoggerFactory.getLogger(NettyConsumer.class);

    private final NettyConsumerConfig consumerConfig;

    public NettyConsumer() {
        this(new NettyConsumerConfig());
    }

    public NettyConsumer(NettyConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
        NettyClientConfig rpcConfig = consumerConfig.getRpcConfig();
        if (rpcConfig == null) {
            rpcConfig = new NettyClientConfig();
        }
        this.rpcClient = new NettyClient(rpcConfig);
        NettyClientConfig registryConfig = consumerConfig.getRegistryConfig();
        if (registryConfig != null) {
            register(registryConfig.getDefaultAddress().getHost(), registryConfig.getDefaultAddress().getPort());
        }
    }

    @Override
    public ConnectWatch watchConnect(final ServiceMetadata serviceMetadata) throws InterruptedException, ConnectionException {
        ConnectWatch connectWatch = new ConnectWatch() {

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
                                        }
                                    }
                                }
                            } catch (InterruptedException | ConnectionException e) {
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
        connectWatch.start();
        return connectWatch;
    }

    public void register(String host, int port) {
        resister(new RemoteAddress(host, port));
    }

    public void resister(UnresolvedAddress unresolvedAddress) {
        NettyClientConfig registryConfig = new NettyClientConfig();
        registryConfig.setDefaultAddress(unresolvedAddress);
        consumerConfig.setRegistryConfig(registryConfig);
        this.registerClient = new ConsumerClient(registryConfig, this);
        this.registerClient.registerProcessors(Protocol.Code.RESPONSE_SUBSCRIBE,
                new NettyConsumerSubscribeProcessor(notifyListeners),
                new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("consumer subscribe")));
        this.registerClient.registerProcessors(Protocol.Code.BROADCAST,
                new NettyConsumerBroadcastProcessor(),
                new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("consumer broadcast")));
    }

    public void directService(ServiceMetadata serviceMetadata, String host, int port) throws InterruptedException, ConnectionException {
        directService(serviceMetadata, new RemoteAddress(host, port));
    }

    public void directService(ServiceMetadata serviceMetadata, UnresolvedAddress socketAddress) throws InterruptedException, ConnectionException {
        SConnector connector = connector(serviceMetadata, socketAddress);
        Channel channel = connector.createChannel();
        connector.channelGroup().add(channel);
    }

    @Override
    public void start() throws InterruptedException {
        if (logger.isDebugEnabled()) {
            logger.info("started rpc client");
        }
        rpcClient.start();

        if (registerClient != null) {

            if (logger.isDebugEnabled()) {
                logger.info("started register client");
            }
            registerClient.start();
            registerConnector = registerClient.connect(new NettyChannelGroup());
            registerConnector.setReConnect(true);
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

    }

    @Override
    public Object call(ServiceMetadata serviceMetadata, String serviceName, String methodName, Class<?> returnType, Object[] args) {
        FilterContext filterContext = callContext(serviceMetadata, serviceName, methodName, args, returnType, false);
        return filterContext.getResult();
    }

    @Override
    public InvokeFuture<?> asyncCall(ServiceMetadata serviceMetadata, String serviceName, String methodName, Class<?> returnType, Object[] args) {
        FilterContext filterContext = callContext(serviceMetadata, serviceName, methodName, args, returnType, true);
        return filterContext.getFuture();
    }

    private FilterContext callContext(ServiceMetadata serviceMetadata, String serviceName, String methodName, Object[] args, Class<?> returnType, boolean async) {
        // 1.找到订阅的服务地址
        ConcurrentHashMap<UnresolvedAddress, SConnector> serviceGroups = subscribeAddress.get(serviceMetadata);
        if (Collections.isEmpty(serviceGroups)) {
            throw new IllegalArgumentException("service provider: " + serviceMetadata.getServiceProviderName() + " can't find");
        }
        LoadBalancer loadBalancer = new BaseLoadBalancer(serviceGroups);
        loadBalancer.setRule(consumerConfig.getRule());
        SConnector connector = loadBalancer.selectServer();
        if (connector == null) {
            throw new IllegalArgumentException("load balance error, maybe service isn't useful.");
        }
        // 2.构建调用链
        Invoker invoker = new RpcInvoker(connector.channelGroup(), async);
        FilterContext filterContext = new FilterContext(invoker, rpcClient);
        filterContext.setReturnType(returnType);
        FilterChain chain = FilterChainLoader.loadChain(new RequestFilter(methodName, args), new InvokerFilter());
        if (chain == null) {
            throw new IllegalArgumentException("error invoker rpc!!!");
        }
        try {
            // 3.从调用链中获取结果
            RequestBody requestBody = new RequestBody();
            requestBody.setServiceName(serviceName);
            chain.process(requestBody, filterContext);
        } catch (InterruptedException | RemoteTimeoutException | RemoteSendException e) {
            logger.error("invoke rpc errors: {}", e.getMessage());
            e.printStackTrace();
        }
        return filterContext;
    }
}
