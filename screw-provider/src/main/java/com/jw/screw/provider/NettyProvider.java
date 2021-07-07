package com.jw.screw.provider;

import com.jw.screw.common.ConnectionCallback;
import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.config.RegisterConfig;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.model.MessageNonAck;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.transport.body.PublishBody;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.monitor.api.ScrewMonitor;
import com.jw.screw.provider.model.ServiceWrapper;
import com.jw.screw.remote.NonAckScanner;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.NettyChannelGroup;
import com.jw.screw.remote.netty.NettyServer;
import com.jw.screw.remote.netty.SConnector;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import com.jw.screw.remote.netty.config.NettyServerConfig;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * screw
 * @author jiangw
 * @date 2020/12/10 14:13
 * @since 1.0
 */
public class NettyProvider implements Provider {

    private static Logger logger = LoggerFactory.getLogger(NettyProvider.class);

    /**
     * 配置
     */
    protected final NettyProviderConfig providerConfig;

    /**
     * 作为服务器端的provider，响应rpc
     */
    protected NettyServer rpcServer;

    /**
     * rpc调用的线程池
     */
    private ExecutorService rpcExec;

    /**
     * 作为客户端的provider，响应注册中心
     */
    private ProviderClient registryClient;

    /**
     * 处理注册中心的线程池，定时向注册中心发布注册信息
     */
    private ExecutorService registerExec;

    /**
     * 对于那些处理失败的，定时发送。
     */
    private ScheduledExecutorService registerScheduled;

    /**
     * provider存储的服务编织管理
     */
    protected ServiceWrapperManager serviceWrapperManager;

    /**
     * 发布的服务，缓存一份到内存中
     */
    private LinkedBlockingQueue<ServiceWrapper> wrappers;

    /**
     * 未接受到的Ack，每个一段时间重试
     */
    private final ConcurrentHashMap<Long, MessageNonAck> nonAck  = new ConcurrentHashMap<>();

    /**
     * 注册中心连接器
     */
    private SConnector registryConnector;

    /**
     * 发布服务的元数据
     */
    private final ServiceMetadata serviceMetadata;

    /**
     * 发布服务的缓存，用于重连再次发送
     */
    private final CopyOnWriteArrayList<Object> publishCached = new CopyOnWriteArrayList<>();

    /**
     * 关闭标识
     */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * 监控器客户端
     */
    private ScrewMonitor monitor;

    public NettyProvider() {
        this(new NettyProviderConfig());
    }

    public NettyProvider(String providerServiceName) {
        NettyProviderConfig providerConfig = new NettyProviderConfig();
        providerConfig.setServerKey(providerServiceName);
        this.providerConfig = providerConfig;
        this.serviceMetadata = new ServiceMetadata(providerServiceName);
        initialize();
    }

    public NettyProvider(NettyProviderConfig providerConfig) {
        String providerKey = providerConfig.getServerKey();
        if (StringUtils.isEmpty(providerKey)) {
            try {
                providerKey = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
            providerConfig.setServerKey(providerKey);
        }
        this.serviceMetadata = new ServiceMetadata(providerKey);
        this.providerConfig = providerConfig;
        NettyClientConfig registerConfig = providerConfig.getRegisterConfig();
        if (registerConfig != null) {
            registry(registerConfig.getDefaultAddress());
        }
        initialize();
    }

    protected void initialize() {
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
        rpcServer = new NettyServer(providerConfig.getRpcServerConfig()) {
            // 处理服务端的Ack信息
            @Override
            protected void processAck(RemoteTransporter remoteTransporter) {
                super.processAck(remoteTransporter);
            }
        };
        rpcExec = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("rpc invoker"));
        registerExec = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(), new NamedThreadFactory("register"));
        registerScheduled = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("register scheduled"));
        registerProcessor();
    }

    @Override
    public void start() throws InterruptedException, ConnectionException, ExecutionException {
        if (logger.isInfoEnabled()) {
            logger.info("----- started provider -----");
        }
        // 获取需要的发布的服务
        wrappers = serviceWrapperManager.wrapperContainer().wrappers();
        if (registryClient != null) {
            registryClient.start();
            registryClient.connect(new NettyChannelGroup(), new ConnectionCallback(null) {
                @Override
                public void acceptable(Object accept) throws ConnectionException {
                    if (accept != null) {
                        registryConnector = (SConnector) accept;
                    }
                }
            });
        }
        // 连接注册中心，启动重连机制。
        registryConnector.setReConnect(true);
        // 初始化发布空服务
        PublishBody publishBody = new PublishBody();
        publishBody.setPublishAddress(providerConfig.getRpcServerConfig().getRegisterAddress());
        publishBody.setServiceName(serviceMetadata.getServiceProviderName());
        publishBody.setConnCount(providerConfig.getConnCount());
        publishBody.setWight(providerConfig.getWeight());
        RemoteTransporter remoteTransporter = RemoteTransporter.createRemoteTransporter(Protocol.Code.SERVICE_REGISTER, publishBody);
        Channel channel = registryConnector.createChannel();
        channel.writeAndFlush(remoteTransporter);
        // 定时发送发布服务到注册中心
        if (registerExec != null) {
            registerExec.submit(new Runnable() {
                @Override
                public void run() {
                    while (!shutdown.get()) {
                        ServiceWrapper wrapper = null;
                        try {
                            if (wrappers == null) {
                                Thread.sleep(3000);
                                wrappers = getServiceWrapperManager().wrapperContainer().wrappers();
                            }
                            wrapper = wrappers.take();
                            Channel channel = registryConnector.createChannel();
                            // 组装发送对象
                            PublishBody publishBody = new PublishBody();
                            publishBody.setPublishAddress(providerConfig.getRpcServerConfig().getRegisterAddress());
                            publishBody.setServiceName(serviceMetadata.getServiceProviderName());
                            publishBody.setConnCount(providerConfig.getConnCount());
                            publishBody.setWight(providerConfig.getWeight());
                            String serviceName = wrapper.getServiceName();
                            publishBody.setPublishServices(serviceName);
                            RemoteTransporter remoteTransporter = RemoteTransporter.createRemoteTransporter(Protocol.Code.SERVICE_REGISTER, publishBody);
                            // 添加到NonAck中
                            MessageNonAck messageNonAck = new MessageNonAck();
                            messageNonAck.setUnique(remoteTransporter.getUnique());
                            messageNonAck.setChannel(channel);
                            messageNonAck.setBody(publishBody);
                            nonAck.put(messageNonAck.getUnique(), messageNonAck);
                            // 传输到注册中心
                            channel.writeAndFlush(remoteTransporter);
                        } catch (InterruptedException | ConnectionException e) {
                            if (wrapper != null) {
                                logger.warn("register service: {} error. prepare retry...", wrapper);
                                final ServiceWrapper finalWrapper = wrapper;
                                // 延迟后再次发送
                                if (registerScheduled != null) {
                                    registerScheduled.schedule(new Runnable() {
                                        @Override
                                        public void run() {
                                            wrappers.add(finalWrapper);
                                        }
                                    }, RegisterConfig.delayPublish, RegisterConfig.delayUnit);
                                }
                            }
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        if (StringUtils.isNotEmpty(providerConfig.getMonitorServerKey())) {
            monitor = new ScrewMonitor(registryConnector, providerConfig);
            registryClient.setMonitor(monitor);
        }
        // 启动服务器
        rpcServer.start();
    }

    @Override
    public void shutdown() throws InterruptedException {
        if (logger.isInfoEnabled()) {
            logger.info("started closed provider service...");
        }
        rpcServer.shutdownGracefully();

        if (registryClient != null) {
            registryClient.shutdownGracefully();
        }

        if (!shutdown.getAndSet(true)) {
            if (rpcExec != null) {
                rpcExec.shutdown();
            }

            if (registerExec != null) {
                registerExec.shutdown();
            }

            if (registerScheduled != null) {
                registerScheduled.shutdown();
            }

        }
        if (logger.isInfoEnabled()) {
            logger.info("closed provider successful!");
        }

        if (monitor != null) {
            monitor.shutdown();
        }
    }

    @Override
    public void registerProcessor() {
        NettyProviderRequestDispatcher dispatcher = new NettyProviderRequestDispatcher(this);
        if (rpcServer != null) {
            rpcServer.registerProcessors(Protocol.Code.RPC_REQUEST, dispatcher, rpcExec);
        }
    }

    @Override
    public void publishServices(Object... services) {
        serviceWrapperManager.register(services);
        // 保存一份在缓存
        publishCached.addAll(Arrays.asList(services));
    }

    @Override
    public void registry(String host, int port) {
        registry(new RemoteAddress(host, port));
    }

    @Override
    public void registry(UnresolvedAddress registryAddress) {
        NettyClientConfig registerConfig = new NettyClientConfig();
        registerConfig.setDefaultAddress(registryAddress);
        if (providerConfig.getRegisterConfig() == null) {
            providerConfig.setRegisterConfig(registerConfig);
        }
        registryClient = new ProviderClient(providerConfig, this);
    }

    public ConcurrentHashMap<Long, MessageNonAck> getNonAck() {
        return nonAck;
    }

    public CopyOnWriteArrayList<Object> getPublishCached() {
        return publishCached;
    }

    public ServiceWrapperManager getServiceWrapperManager() {
        return serviceWrapperManager;
    }

    public LinkedBlockingQueue<ServiceWrapper> getWrappers() {
        return wrappers;
    }

    public NettyProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public SConnector getRegistryConnector() {
        return registryConnector;
    }

    public AtomicBoolean getShutdown() {
        return shutdown;
    }

    {
        // 处理NonAck
        ScheduledExecutorService ackScheduled = Executors.newScheduledThreadPool(1, new NamedThreadFactory("provider service Non Ack", true));
        ackScheduled.scheduleAtFixedRate(new NonAckScanner(nonAck), RegisterConfig.delayPublish, RegisterConfig.delayPeriod, RegisterConfig.delayUnit);
    }
}
