package com.jw.screw.remote.netty.handler;

import com.jw.screw.remote.modle.HeartBeats;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiangw
 */
@ChannelHandler.Sharable
public class ClientIdleTrigger extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ClientIdleTrigger.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                if (logger.isDebugEnabled()) {
                    logger.debug("client write idle... started send heart beats data package");
                }
                ctx.channel().writeAndFlush(HeartBeats.heartbeat());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
