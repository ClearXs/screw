package com.jw.screw.remote;

import com.jw.screw.common.model.MessageNonAck;
import com.jw.screw.common.transport.body.Body;
import com.jw.screw.common.transport.body.MonitorBody;
import com.jw.screw.common.transport.body.OfflineBody;
import com.jw.screw.common.transport.body.RegisterBody;
import com.jw.screw.common.util.Collections;
import com.jw.screw.remote.modle.RemoteTransporter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiangw
 */
public class NonAckScanner implements Runnable {

    ConcurrentHashMap<Long, MessageNonAck> nonAck;

    public NonAckScanner(ConcurrentHashMap<Long, MessageNonAck> nonAck) {
        this.nonAck = nonAck;
    }

    @Override
    public void run() {
        ConcurrentHashMap<Long, MessageNonAck> unActiveMessage = new ConcurrentHashMap<>();
        for (MessageNonAck messageNonAck : nonAck.values()) {
            long unique = messageNonAck.getUnique();
            // 如果移除为空，说明被其他线程移除，或者响应Ack时移除
            if (nonAck.remove(unique) == null) {
                continue;
            }
            // 重新构建transport
            Body body = messageNonAck.getBody();
            byte code = Protocol.Code.FAILED;
            if (body instanceof RegisterBody) {
                code = Protocol.Code.RESPONSE_SUBSCRIBE;
            } else if (body instanceof OfflineBody) {
                code = Protocol.Code.SERVICE_OFFLINE;
            } else if (body instanceof MonitorBody) {
                code = Protocol.Code.UNICAST;
            }
            RemoteTransporter transporter = RemoteTransporter.createRemoteTransporter(code
                    , body, messageNonAck.getUnique());
            Channel channel = messageNonAck.getChannel();
            if (channel.isActive()) {
                messageNonAck.getChannel()
                        .writeAndFlush(transporter)
                        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            } else {
                unActiveMessage.put(unique, messageNonAck);
            }
        }
        // 移除这个message
        if (Collections.isNotEmpty(unActiveMessage)) {
            Enumeration<Long> keys = unActiveMessage.keys();
            while (keys.hasMoreElements()){
                Long key = keys.nextElement();
                nonAck.remove(key);
            }
        }
    }
}
