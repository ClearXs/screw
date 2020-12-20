package com.jw.screw.consumer.process;

import com.jw.screw.common.transport.body.AcknowledgeBody;
import com.jw.screw.common.transport.body.MonitorBody;
import com.jw.screw.consumer.Listeners;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;

/**
 * 广播通知处理器
 * @author jiangw
 * @date 2020/12/8 17:05
 * @since 1.0
 */
public class NettyConsumerBroadcastProcessor implements NettyProcessor {

    @Override
    public RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request) {
        MonitorBody monitorBody = (MonitorBody) request.getBody();
        // 通知监听的消费者
        try {
            Listeners.notifyFuture(monitorBody);
            // 构建ack消息
            AcknowledgeBody acknowledgeBody = new AcknowledgeBody(request.getUnique(), true);
            return RemoteTransporter.createRemoteTransporter(Protocol.Code.UNKNOWN, acknowledgeBody, request.getUnique(), Protocol.TransportType.ACK);
        } catch (ClassNotFoundException e) {
            // 发生异常时，不发送Ack信息，等待注册中心再次发送。
            e.printStackTrace();
        }
        return null;
    }
}
