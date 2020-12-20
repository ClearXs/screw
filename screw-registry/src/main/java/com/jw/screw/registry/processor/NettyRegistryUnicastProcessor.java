package com.jw.screw.registry.processor;

import com.jw.screw.common.model.MessageNonAck;
import com.jw.screw.common.transport.body.AcknowledgeBody;
import com.jw.screw.common.transport.body.MonitorBody;
import com.jw.screw.common.util.Collections;
import com.jw.screw.registry.NettyRegistryServer;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 注册中心对提供者单播的处理器
 * @author jiangw
 * @date 2020/12/8 15:10
 * @since 1.0
 */
public class NettyRegistryUnicastProcessor implements NettyProcessor {

    private final NettyRegistryServer registryServer;

    public NettyRegistryUnicastProcessor(NettyRegistryServer registryServer) {
        this.registryServer = registryServer;
    }

    @Override
    public RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request) {
        // 解析提供者的服务是否存在于注册中心
        MonitorBody monitorBody = (MonitorBody) request.getBody();
        CopyOnWriteArraySet<Channel> channels = registryServer.handleUnicast(monitorBody);
        // 发送广播消息（保证消息的正确发送）
        if (Collections.isNotEmpty(channels)) {
            for (Channel channel : channels) {
                RemoteTransporter broadcastMessage = RemoteTransporter.createRemoteTransporter(Protocol.Code.BROADCAST, monitorBody);
                MessageNonAck messageNonAck = new MessageNonAck();
                messageNonAck.setBody(monitorBody);
                messageNonAck.setUnique(broadcastMessage.getUnique());
                messageNonAck.setChannel(channel);
                channel.writeAndFlush(broadcastMessage);
            }
        }
        AcknowledgeBody acknowledgeBody = new AcknowledgeBody(request.getUnique(), true);
        return RemoteTransporter
                .createRemoteTransporter(Protocol.Code.UNKNOWN, acknowledgeBody, request.getUnique(), Protocol.TransportType.ACK);
    }
}
