package com.jw.screw.common.model;

import com.jw.screw.common.transport.body.Body;
import io.netty.channel.Channel;

/**
 * 还未确认的消息
 * @author jiangw
 * @date 2020/12/10 17:32
 * @since 1.0
 */
public class MessageNonAck {

    private long unique;

    /**
     * 注册消息、消费消息、提供消息
     */
    private Body body;

    /**
     * 远端连接的通道
     */
    private Channel channel;

    public MessageNonAck() {
    }

    public MessageNonAck(long unique, Body body, Channel channel) {
        this.unique = unique;
        this.body = body;
        this.channel = channel;
    }

    public long getUnique() {
        return unique;
    }

    public void setUnique(long unique) {
        this.unique = unique;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
