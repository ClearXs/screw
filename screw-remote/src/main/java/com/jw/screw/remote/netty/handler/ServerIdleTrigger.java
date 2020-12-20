package com.jw.screw.remote.netty.handler;

import com.jw.screw.remote.modle.HeartBeats;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * screw
 * @author jiangw
 * @date 2020/12/8 20:10
 * @since 1.0
 */
@ChannelHandler.Sharable
public class ServerIdleTrigger extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ServerIdleTrigger.class);

    private final ThreadLocal<AtomicInteger> threadLocalCounter = new ThreadLocal<>();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            AtomicInteger readTimeoutCount = threadLocalCounter.get();
            threadLocalCounter.remove();
            if (readTimeoutCount == null) {
                readTimeoutCount = new AtomicInteger(0);
            }
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                if (readTimeoutCount.getAndIncrement() >= HeartBeats.config().getAccept()) {
                    readTimeoutCount.set(0);
                    if (logger.isWarnEnabled()) {
                        logger.warn("freed channel: {}, ", ctx.channel());
                    }
                    ctx.fireChannelInactive();
                }
                threadLocalCounter.set(readTimeoutCount);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
