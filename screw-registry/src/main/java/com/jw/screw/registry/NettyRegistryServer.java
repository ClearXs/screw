package com.jw.screw.registry;

import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.model.MessageNonAck;
import com.jw.screw.common.model.Tuple;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.transport.body.*;
import com.jw.screw.common.util.Collections;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.NettyServer;
import com.jw.screw.remote.netty.config.NettyServerConfig;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

/**
 * screw
 * @author jiangw
 * @date 2020/12/11 9:32
 * @since 1.0
 */
public class NettyRegistryServer extends NettyServer {

    private static Logger logger = LoggerFactory.getLogger(NettyRegistryServer.class);

    /**
     * 与Registry公用的上下文对象
     */
    private final RegistryContext registryContext;

    /**
     * 生产者通道已经发布的服务
     */
    private final static AttributeKey<CopyOnWriteArraySet<RegisterMetadata>> PUBLISH_KEY = AttributeKey.valueOf("publishKey");

    /**
     * 消费者通道订阅的服务
     */
    private final static AttributeKey<ServiceMetadata> SUBSCRIBE_KEY = AttributeKey.valueOf("subscribeKey");

    public NettyRegistryServer(NettyServerConfig serverConfig, RegistryContext registryContext) {
        super(serverConfig);
        this.registryContext = registryContext;
    }

    /**
     * 处理服务注册、订阅
     * @param ctx
     * @param request
     */
    @Override
    protected void processRemoteRequest(ChannelHandlerContext ctx, RemoteTransporter request) {
        byte code = request.getCode();
        Tuple<NettyProcessor, ExecutorService> processorTuple = processTables.get(code);
        if (processorTuple == null) {
            // 发送重试的Ack消息
            sendRetryAck(ctx, request);
            return;
        }
        final NettyProcessor processor = processorTuple.getKey();
        ExecutorService executorService = processorTuple.getValue();
        switch (code) {
            case Protocol.Code.SERVICE_REGISTER:
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        // 返回Ack的信息
                        RemoteTransporter ackTransport = processor.process(ctx, request);
                        ctx.channel().writeAndFlush(ackTransport).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                    }
                });
                break;
            case Protocol.Code.SERVICE_SUBSCRIBE:
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        // 存储消费者的通道信息，便于之后通知
                        SubscribeBody subscribeBody = (SubscribeBody) request.getBody();
                        CopyOnWriteArraySet<Channel> channels = registryContext.getSubscribeChannels().get(subscribeBody.getServiceMetadata().getServiceProviderName());
                        if (channels == null) {
                            channels = new CopyOnWriteArraySet<>();
                            registryContext.getSubscribeChannels().put(subscribeBody.getServiceMetadata().getServiceProviderName(), channels);
                        }
                        Channel consumerChannel = ctx.channel();
                        channels.add(consumerChannel);
                        // 消费者与注册中心连接的channel
                        Attribute<ServiceMetadata> attr = ctx.channel().attr(SUBSCRIBE_KEY);
                        attr.set(subscribeBody.getServiceMetadata());
                        // 设置未发布消息，等待消费者Ack
                        RemoteTransporter response = processor.process(ctx, request);
                        RegisterBody registerBody = (RegisterBody) response.getBody();
                        MessageNonAck messageNonAck = new MessageNonAck();
                        messageNonAck.setBody(registerBody);
                        messageNonAck.setUnique(request.getUnique());
                        messageNonAck.setChannel(consumerChannel);
                        registryContext.getNonAck().put(messageNonAck.getUnique(), messageNonAck);

                        ctx.channel().writeAndFlush(response);
                    }
                });
                break;
            case Protocol.Code.UNICAST:
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        RemoteTransporter ackTransport = processor.process(ctx, request);
                        ctx.channel().writeAndFlush(ackTransport).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                    }
                });
            default:
                // code不正确，发送错误的消息
                sendRetryAck(ctx, request);
                break;
        }
    }

    @Override
    protected void processAck(RemoteTransporter remoteTransporter) {
        AcknowledgeBody body = (AcknowledgeBody) remoteTransporter.getBody();
        boolean success = body.isSuccess();
        if (success) {
            // 移除
            registryContext.getNonAck().remove(body.getSequence());
        }
    }

    /**
     * 处理服务注册
     * @param unresolvedAddress
     * @param registerMetadata
     * @param acknowledgeBody
     */
    public void handleRegister(Channel channel, UnresolvedAddress unresolvedAddress, RegisterMetadata registerMetadata, AcknowledgeBody acknowledgeBody) {
        try {
            ServiceMetadata serviceMetadata = new ServiceMetadata(registerMetadata.getServiceProviderName());
            synchronized (registryContext.getGlobalRegisterInfo()) {
                // 保证当前注册数据存在于这个通道内
                boolean notify = registerService(channel, unresolvedAddress, registerMetadata);
                if (notify) {
                    // 向所有消费者发布消息
                    ConcurrentHashMap<String, CopyOnWriteArraySet<Channel>> subscribeChannels = registryContext.getSubscribeChannels();
                    if (Collections.isEmpty(subscribeChannels)) {
                        return;
                    }
                    CopyOnWriteArraySet<Channel> channels = subscribeChannels.get(registerMetadata.getServiceProviderName());
                    if (Collections.isEmpty(channels)) {
                        return;
                    }
                    ConcurrentHashMap<UnresolvedAddress, RegisterMetadata> registerInfo = registryContext.getRegisterInfo(registerMetadata.getServiceProviderName());
                    for (Channel subscribeChannel : channels) {
                        RegisterBody registerBody = new RegisterBody(serviceMetadata.getServiceProviderName(), Collections.newArrayList(registerInfo.values()));
                        RemoteTransporter remoteTransporter = RemoteTransporter.createRemoteTransporter(Protocol.Code.RESPONSE_SUBSCRIBE, registerBody);
                        subscribeChannel.writeAndFlush(remoteTransporter);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("register error: {}", e.getMessage());
            acknowledgeBody.setSuccess(false);
        }
    }

    /**
     * 把服务注册到注册中心的服务实例表中
     * 若不第一次注册，返回空
     * @param channel
     * @param unresolvedAddress
     * @param registerMetadata
     * @return 是否通知消费者 服务更新 true
     */
    public boolean registerService(Channel channel, UnresolvedAddress unresolvedAddress, RegisterMetadata registerMetadata) {
        if (logger.isInfoEnabled()) {
            logger.info("register unresolvedAddress: {}, serviceProviderName: {}, publishService: {}", unresolvedAddress, registerMetadata.getServiceProviderName(), registerMetadata.getPublishService());
        }
        ServiceMetadata serviceMetadata = new ServiceMetadata(registerMetadata.getServiceProviderName());
        ConcurrentHashMap<UnresolvedAddress, RegisterMetadata> registerInfo = registryContext.getRegisterInfo(registerMetadata.getServiceProviderName());
        if (registerInfo.putIfAbsent(unresolvedAddress, registerMetadata) == null) {
            registerMetadata.setChannel(channel);
            // 保存服务提供者列表
            CopyOnWriteArraySet<UnresolvedAddress> unresolvedAddresses = registryContext.getServiceInfo(serviceMetadata);
            unresolvedAddresses.add(unresolvedAddress);
            Attribute<CopyOnWriteArraySet<RegisterMetadata>> publishAttr = channel.attr(PUBLISH_KEY);

            CopyOnWriteArraySet<RegisterMetadata> registerSet = publishAttr.get();
            if (Collections.isEmpty(registerSet)) {
                registerSet = new CopyOnWriteArraySet<>();
                publishAttr.set(registerSet);
            }
            registerSet.add(registerMetadata);
            return true;
        }
        // 如果注册实例已经存在于注册中心，那么此时可能是服务提供者再次与注册中心连接，需要重新更新channel
        RegisterMetadata instanceRegister = registerInfo.get(unresolvedAddress);
        if (instanceRegister != null) {
            instanceRegister.setChannel(channel);
        }
        // 判断是否有同一个服务加入
        return Collections.union(instanceRegister.getPublishService(), registerMetadata.getPublishService());
    }

    /**
     * 处理消费的订阅
     * @param serviceProviders
     */
    public List<RegisterMetadata> handleSubscribe(ServiceMetadata... serviceProviders) {
        List<RegisterMetadata> registerMetadata = new ArrayList<>();
        for (ServiceMetadata serviceMetadata : serviceProviders) {
            ConcurrentHashMap<UnresolvedAddress, RegisterMetadata> registerInfo = registryContext.getRegisterInfo(serviceMetadata.getServiceProviderName());
            registerMetadata.addAll(Collections.newArrayList(registerInfo.values()));
        }
        return registerMetadata;
    }

    public CopyOnWriteArraySet<Channel> handleUnicast(MonitorBody monitorBody) {
        String providerKey = monitorBody.getProviderKey();
        String serviceName = monitorBody.getServiceName();

        // 判断当前单播的服务是否存在于注册中心
        boolean serviceIsExist = false;
        ConcurrentHashMap<UnresolvedAddress, RegisterMetadata> registerMap = registryContext.getGlobalRegisterInfo().get(providerKey);
        if (Collections.isEmpty(registerMap)) {
            return null;
        }
        for (Map.Entry<UnresolvedAddress, RegisterMetadata> registerEntry : registerMap.entrySet()) {
            RegisterMetadata registerMetadata = registerEntry.getValue();
            List<String> publishService = registerMetadata.getPublishService();
            boolean contains = publishService.contains(serviceName);
            if (contains) {
                serviceIsExist = true;
                break;
            }
        }
        if (!serviceIsExist) {
            return null;
        }

        // 查找订阅该服务的consumer
        return registryContext.getSubscribeChannels().get(providerKey);
    }

    /**
     * 处理提供者下线操作，删除对应的服务
     * @param recordDeletedInstance
     */
    private void handleRemovedService(ConcurrentHashMap<UnresolvedAddress, RegisterMetadata> recordDeletedInstance) {
        synchronized (registryContext.getGlobalRegisterInfo()) {
            for (Map.Entry<UnresolvedAddress, RegisterMetadata> deletedEntry : recordDeletedInstance.entrySet()) {
                ConcurrentHashMap<UnresolvedAddress, RegisterMetadata> registerInfo = registryContext.getRegisterInfo(deletedEntry.getValue().getServiceProviderName());
                RegisterMetadata deleted = registerInfo.remove(deletedEntry.getKey());
                if (deleted == null) {
                    continue;
                }
                ServiceMetadata serviceMetadata = new ServiceMetadata(deleted.getServiceProviderName());
                UnresolvedAddress unresolvedAddress = deleted.getUnresolvedAddress();
                // 删除服务表
                CopyOnWriteArraySet<UnresolvedAddress> serviceInfo = registryContext.getServiceInfo(serviceMetadata);
                if (Collections.isNotEmpty(serviceInfo)) {
                    if (serviceInfo.remove(unresolvedAddress)) {
                        // 删除服务器通道
                        registryContext.getRegisterChannels().remove(deleted.getUnresolvedAddress());
                    }
                }
            }

        }
    }

    private void handleOffline(RegisterMetadata registerMetadata) {
        // 向所有的消费者发送相关服务下线通知
        Collection<CopyOnWriteArraySet<Channel>> subscribeChannels = registryContext.getSubscribeChannels().values();
        if (Collections.isEmpty(subscribeChannels)) {
            return;
        }
        for (CopyOnWriteArraySet<Channel> channels : subscribeChannels) {
            if (Collections.isEmpty(channels)) {
                continue;
            }
            // 创建下线通知的数据
            OfflineBody offlineBody = new OfflineBody(registerMetadata);
            for (Channel channel : channels) {
                // 记录到MessageNonAck
                RemoteTransporter remoteTransporter = RemoteTransporter.createRemoteTransporter(Protocol.Code.SERVICE_OFFLINE, offlineBody);
                MessageNonAck messageNonAck = new MessageNonAck();
                messageNonAck.setBody(offlineBody);
                messageNonAck.setChannel(channel);
                messageNonAck.setUnique(remoteTransporter.getUnique());
                registryContext.getNonAck().put(remoteTransporter.getUnique(), messageNonAck);

                channel.writeAndFlush(remoteTransporter)
                        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
        }
    }

    /**
     *  1.服务提供者下线，通过心跳机制，如果服务提交者长时间没有向注册中心发送信息，那么就认为服务已经挂掉。
     *  注：
     *     存在一种情况，当服务宕机或者网络原因导致进行重连时会开启一个新的通道，这导致上一个通道长时间没有收到心跳包导致通道关闭。
     *     由于上一次通道里面存放在服务实例的信息，导致注册中心把相应的实例删除。而重连之后，由于实例表有这个key，所以也不会添加。
     *
     * 2.消费者下线，同服务下线
     * 注：
     *    因为每次消费者订阅都会把与注册中心之间的通道放入注册中心，这样也会导致与服务下线一样的情况。
     */
    @Override
    protected ChannelHandler extraHandler() {

        return new ChannelInboundHandlerAdapter() {
            @Override
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                Channel channel = ctx.channel();
                // ----- 服务提供者下线
                Attribute<CopyOnWriteArraySet<RegisterMetadata>> publishAttr = channel.attr(PUBLISH_KEY);
                CopyOnWriteArraySet<RegisterMetadata> registerSet = publishAttr.get();
                if (Collections.isNotEmpty(registerSet)) {
                    for (RegisterMetadata registerMetadata : registerSet) {
                        // 服务注册表中管道与当前管道是否一致
                        ConcurrentHashMap<UnresolvedAddress, RegisterMetadata> registerMap = registryContext.getGlobalRegisterInfo().get(registerMetadata.getServiceProviderName());
                        // 记录待删除注册实例表
                        ConcurrentHashMap<UnresolvedAddress, RegisterMetadata> recordDeletedInstance = new ConcurrentHashMap<>();
                        // 当前registerMetadata是否通过验证
                        boolean deleted = false;
                        for (Map.Entry<UnresolvedAddress, RegisterMetadata> registerEntry : registerMap.entrySet()) {
                            RegisterMetadata instanceRegister = registerEntry.getValue();
                            if (instanceRegister.getChannel().id().equals(channel.id()) &&
                                    instanceRegister.getServiceProviderName().equals(registerMetadata.getServiceProviderName())) {
                                recordDeletedInstance.put(registerEntry.getKey(), registerEntry.getValue());
                                deleted = true;
                            }
                        }
                        if (deleted) {
                            // 从注册中心移除相应的注册、服务表
                            handleRemovedService(recordDeletedInstance);

                            // 向消费者发送服务下线通知
                            handleOffline(registerMetadata);

                            // 取消在channel标记attr
                            Attribute<CopyOnWriteArraySet<RegisterMetadata>> attr = ctx.channel().attr(PUBLISH_KEY);
                            CopyOnWriteArraySet<RegisterMetadata> register = attr.get();
                            if (register != null) {
                                register.remove(registerMetadata);
                            }
                        }
                    }
                }

                // ----- 消费者下线
                Attribute<ServiceMetadata> subscribeAttr = ctx.channel().attr(SUBSCRIBE_KEY);
                ServiceMetadata serviceMetadata = subscribeAttr.get();
                if (serviceMetadata != null) {
                    // 注册中心移除保存的消费者channel
                    ConcurrentHashMap<String, CopyOnWriteArraySet<Channel>> subscribeChannels = registryContext.getSubscribeChannels();
                    if (Collections.isNotEmpty(subscribeChannels)) {
                        for (Map.Entry<String, CopyOnWriteArraySet<Channel>> subscribeEntry : subscribeChannels.entrySet()) {
                            CopyOnWriteArraySet<Channel> instanceConsumerChannels = subscribeEntry.getValue();
                            instanceConsumerChannels.remove(channel);
                        }
                    }
                }
                // 关闭通道
                channel.close();
            }
        };
    }

}
