package com.jw.screw.remote.netty.processor;

import com.jw.screw.remote.modle.RemoteTransporter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleNettyProcessor implements NettyProcessor {

    private static Logger logger = LoggerFactory.getLogger(SimpleNettyProcessor.class);

    @Override
    public RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request) {
        logger.info("request {}", request);
        return null;
    }
}
