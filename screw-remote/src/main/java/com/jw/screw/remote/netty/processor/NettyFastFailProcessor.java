package com.jw.screw.remote.netty.processor;

import com.jw.screw.remote.modle.RemoteTransporter;
import io.netty.channel.ChannelHandlerContext;

public class NettyFastFailProcessor implements NettyProcessor {

    private Throwable cause;

    public NettyFastFailProcessor(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request) {
        return RemoteTransporter.failedResponse(request.getUnique(), cause);
    }
}
