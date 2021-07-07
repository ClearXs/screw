package com.jw.screw.consumer;

import com.jw.screw.common.NotifyListener;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.model.MessageNonAck;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.transport.body.SubscribeBody;
import com.jw.screw.common.util.Collections;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.NettyChannelGroup;
import com.jw.screw.remote.netty.SConnector;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;

/**
 * @author jiangw
 */
public abstract class AbstractConsumer implements com.jw.screw.consumer.Consumer {

    private static Logger logger = LoggerFactory.getLogger(AbstractConsumer.class);

    /**
     * 作为rpc调用的客户端
     */
    protected com.jw.screw.consumer.RpcClient rpcClient;

    /**
     * 作为注册中心的客户端
     */
    protected com.jw.screw.consumer.ConsumerClient registerClient;

    /**
     * 注册中心的连接器
     */
    protected SConnector registerConnector;

    /**
     * 订阅获取的服务列表
     * key serviceProviderName
     * value [address, connector]...
     */
    protected volatile ConcurrentHashMap<ServiceMetadata, ConcurrentHashMap<UnresolvedAddress, SConnector>> subscribeAddress = new ConcurrentHashMap<>();

    /**
     * 订阅服务缓存，便于重连再次订阅
     */
    protected final CopyOnWriteArraySet<ServiceMetadata> subscribeService = new CopyOnWriteArraySet<>();

    /**
     * 未收到Ack的remote transport object，后台线程定时发送
     */
    protected final ConcurrentHashMap<Long, MessageNonAck> nonAck = new ConcurrentHashMap<>();

    /**
     * 监听的服务地址
     */
    protected final ConcurrentHashMap<ServiceMetadata, CopyOnWriteArraySet<NotifyListener>> notifyListeners = new ConcurrentHashMap<>();

    protected void subscribe(ServiceMetadata serviceMetadata, NotifyListener listener) throws ConnectionException, InterruptedException {
        if (registerClient == null) {
            logger.warn("register client is empty, can't subscribe: {}", serviceMetadata);
            return;
        }
        CopyOnWriteArraySet<NotifyListener> listeners = this.notifyListeners.get(serviceMetadata);
        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<>();
            this.notifyListeners.put(serviceMetadata, listeners);
        }
        listeners.add(listener);
        Channel channel = registerConnector.createChannel();
        SubscribeBody subscribeBody = new SubscribeBody();
        subscribeBody.setServiceMetadata(serviceMetadata);
        RemoteTransporter remoteTransporter = RemoteTransporter.createRemoteTransporter(Protocol.Code.SERVICE_SUBSCRIBE, subscribeBody);
        // 创建NonAck数据
        MessageNonAck messageNonAck = new MessageNonAck();
        messageNonAck.setBody(subscribeBody);
        messageNonAck.setUnique(remoteTransporter.getUnique());
        messageNonAck.setChannel(channel);
        nonAck.put(remoteTransporter.getUnique(), messageNonAck);
        channel.writeAndFlush(remoteTransporter);
    }

    protected SConnector connector(ServiceMetadata serviceMetadata, UnresolvedAddress unresolvedAddress) throws InterruptedException, ExecutionException {
        return connector(serviceMetadata, unresolvedAddress, 4);
    }

    protected synchronized SConnector connector(ServiceMetadata serviceMetadata, UnresolvedAddress unresolvedAddress, int weight) throws InterruptedException, ExecutionException {
        SConnector connector;
        ConcurrentHashMap<UnresolvedAddress, SConnector> serviceGroups = subscribeAddress.get(serviceMetadata);
        if (Collections.isEmpty(serviceGroups)) {
            ConcurrentHashMap<UnresolvedAddress, SConnector> newServiceGroups = new ConcurrentHashMap<>();
            SConnector newConnector = rpcClient.connect(unresolvedAddress, new NettyChannelGroup(), weight, null);
            subscribeAddress.putIfAbsent(serviceMetadata, newServiceGroups);
            connector = newServiceGroups.putIfAbsent(unresolvedAddress, newConnector);
            if (connector == null) {
                connector = newConnector;
            }
        } else {
            connector = serviceGroups.get(unresolvedAddress);
            if (connector == null) {
                SConnector newConnector = rpcClient.connect(unresolvedAddress, new NettyChannelGroup(), weight, null);
                connector = serviceGroups.putIfAbsent(unresolvedAddress, newConnector);
                if (connector == null) {
                    connector = newConnector;
                }
            }
        }
        return connector;
    }

    public CopyOnWriteArraySet<ServiceMetadata> getSubscribeService() {
        return subscribeService;
    }
}
