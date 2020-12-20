package com.jw.screw.remote.netty.handler;

import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.remote.netty.ChannelGroup;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端重连检测
 * @author jiangw
 * @date 2020/11/25 22:42
 * @since 1.0
 */
@ChannelHandler.Sharable
public abstract class ConnectorWatchDog extends ChannelInboundHandlerAdapter implements TimerTask, ChannelHandlerHolder {

    private static Logger logger = LoggerFactory.getLogger(ConnectorWatchDog.class);

    private final static int START = 1;

    private final static int STOP = 2;

    private final Bootstrap bootstrap;

    private final Timer timer;

    private final static int RE_CONNECTION_MAX_NUM = 12;

    private volatile int state = START;

    /**
     * 远程连接地址
     */
    private UnresolvedAddress socketAddress;


    private final ChannelGroup channelGroup;

    /**
     * 第一次连接，需要获取连接地址，所以需要置为true
     */
    private volatile boolean firstConnect = true;

    /**
     * 重连次数，考虑到这个处理器是共享的，所以会出现线程不安全的情况，考虑使用线程本地存储进行操作
     */
    private final ThreadLocal<AtomicInteger> attempts = new ThreadLocal<>();

    public ConnectorWatchDog(Bootstrap bootstrap, Timer timer, ChannelGroup channelGroup) {
        this.bootstrap = bootstrap;
        this.timer = timer;
        this.channelGroup = channelGroup;
    }

    public void start() {
        state = START;
    }

    public void stop() {
        state = STOP;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        attempts.remove();
        attempts.set(new AtomicInteger(0));
        firstConnect = true;
        Channel channel = ctx.channel();
        if (channelGroup != null) {
            channelGroup.add(channel);
        }
        if (logger.isInfoEnabled()) {
            logger.info("connector msg: {}", channel);
        }
        // 通知流水线的处理器重连成功
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!isReConnect()) {
            if (logger.isWarnEnabled()) {
                logger.warn("cancel re connect");
            }
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("client started re connection");
        }
        if (firstConnect) {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            this.socketAddress = new RemoteAddress(socketAddress.getHostString(), socketAddress.getPort());
            firstConnect = false;
        }
        AtomicInteger attempt = attempts.get();
        attempts.remove();
        if (attempt == null) {
            attempt = new AtomicInteger(0);
        }
        if (attempt.getAndIncrement() < RE_CONNECTION_MAX_NUM) {
            long timed = 2L << attempt.get();
            timer.newTimeout(this, timed, TimeUnit.MILLISECONDS);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("client re connection failed, because attempts more than re connection max num");
            }
        }
        attempts.set(attempt);
        ctx.fireChannelInactive();
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        ChannelFuture channelFuture;
        synchronized (bootstrap) {
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(channelHandlers());
                }
            });
            channelFuture = bootstrap.connect(InetSocketAddress.createUnresolved(socketAddress.getHost(), socketAddress.getPort()));
        }
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    if (logger.isInfoEnabled()) {
                        logger.info("client re connection successful");
                    }
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info("client re connection failed, started next times re connection");
                    }
                    future.channel().pipeline().fireChannelInactive();
                }
            }
        });
    }

    private boolean isStarted() {
        return state == START;
    }

    private boolean isReConnect() {
        return isStarted() && channelGroup != null;
    }
}