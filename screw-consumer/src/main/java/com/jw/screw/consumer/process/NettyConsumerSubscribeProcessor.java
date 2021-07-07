package com.jw.screw.consumer.process;

import com.jw.screw.common.NotifyListener;
import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.transport.body.AcknowledgeBody;
import com.jw.screw.common.transport.body.OfflineBody;
import com.jw.screw.common.transport.body.RegisterBody;
import com.jw.screw.common.util.Collections;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * consumer处理订阅信息
 * @author jiangw
 * @date 2020/12/8 15:26
 * @since 1.0
 */
public class NettyConsumerSubscribeProcessor implements NettyProcessor {

    private static Logger logger = LoggerFactory.getLogger(NettyConsumerSubscribeProcessor.class);

    private ConcurrentHashMap<ServiceMetadata, CopyOnWriteArraySet<NotifyListener>> notifyListeners;

    public NettyConsumerSubscribeProcessor(ConcurrentHashMap<ServiceMetadata, CopyOnWriteArraySet<NotifyListener>> notifyListeners) {
        this.notifyListeners = notifyListeners;
    }

    @Override
    public RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request) {
        byte code = request.getCode();
        if (code == Protocol.Code.RESPONSE_SUBSCRIBE) {
            RegisterBody registerBody = (RegisterBody) request.getBody();
            ServiceMetadata serviceMetadata = new ServiceMetadata(registerBody.getServiceProviderName());
            List<RegisterMetadata> registerInfos = registerBody.getRegisterMetadata();
            if (logger.isDebugEnabled()) {
                logger.debug("subscribe server: {}, provider service: {}", serviceMetadata, registerInfos);
            }
            // 如果注册中心返回的生产者发布的服务为空，那么有两种可能：
            // 1.生产者未发布服务到注册中心
            // 2.网络分区
            // 这种情况下，应该有重试策略（暂未实现）
            CopyOnWriteArraySet<NotifyListener> notifyListeners = this.notifyListeners.get(serviceMetadata);
            if (notifyListeners != null) {
                for (NotifyListener listener : notifyListeners) {
                    listener.notify(NotifyListener.NotifyEvent.ADD, registerInfos == null ? null : registerInfos.toArray(new RegisterMetadata[0]));
                }
            }
        } else if (code == Protocol.Code.SERVICE_OFFLINE) {
            OfflineBody body = (OfflineBody) request.getBody();
            RegisterMetadata registerMetadata = body.getRegisterMetadata();
            if (logger.isDebugEnabled()) {
                logger.debug("server offline: {}", registerMetadata);
            }
            // 通知消费者
            ServiceMetadata serviceMetadata = new ServiceMetadata(registerMetadata.getServiceProviderName());
            CopyOnWriteArraySet<NotifyListener> notifyListeners = this.notifyListeners.get(serviceMetadata);
            if (Collections.isNotEmpty(notifyListeners)) {
                for (NotifyListener listener : notifyListeners) {
                    listener.notify(NotifyListener.NotifyEvent.REMOVED, java.util.Collections.singletonList(registerMetadata).toArray(new RegisterMetadata[0]));
                }
            }
        }
        // 构造Ack消息恢复注册中心
        AcknowledgeBody acknowledgeBody = new AcknowledgeBody(request.getUnique(), true);
        return RemoteTransporter.createRemoteTransporter(Protocol.Code.UNKNOWN, acknowledgeBody, request.getUnique(), Protocol.TransportType.ACK);
    }
}
