package com.jw.screw.remote.netty.handler;

import io.netty.channel.ChannelHandler;

public interface ChannelHandlerHolder {

    ChannelHandler[] channelHandlers();
}
