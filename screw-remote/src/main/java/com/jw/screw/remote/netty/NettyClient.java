package com.jw.screw.remote.netty;

import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.remote.modle.HeartBeats;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.codec.RemoteTransporterDecoder;
import com.jw.screw.remote.netty.codec.RemoteTransporterEncoder;
import com.jw.screw.remote.netty.config.GlobeConfig;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import com.jw.screw.remote.netty.handler.ClientIdleTrigger;
import com.jw.screw.remote.netty.handler.ConnectorWatchDog;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 一个netty的客户端
 * @author jiangw
 * @date 2020/11/26 11:44
 * @since 1.0
 */
public class NettyClient extends AbstractNettyService {

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private final NettyClientConfig clientConfig;

    private Bootstrap bootstrap;

    private NioEventLoopGroup worker;

    private final Timer timer = new HashedWheelTimer();

    public NettyClient(NettyClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        init();
    }

    @Override
    public void init() {
        bootstrap = new Bootstrap();
        worker = new NioEventLoopGroup(clientConfig.getWorkThreads(), new DefaultThreadFactory("netty client"));
        bootstrap.group(worker);

        // IO利用率
        worker.setIoRatio(100);
        // options
        // 池化缓存区
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        // 连接超时时间
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, GlobeConfig.CONNECT_TIMEOUT_MILLIS);
        // 地址复用
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        // 长连接
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        // TCP
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        // 关闭连接不发通知
        bootstrap.option(ChannelOption.ALLOW_HALF_CLOSURE, false);
    }

    @Override
    public void start() {
    }

    public SConnector connect(ChannelGroup channelGroup) throws InterruptedException {
        UnresolvedAddress address = clientConfig.getDefaultAddress();
        if (address == null) {
            throw new IllegalArgumentException("default address is empty");
        }
        return connect(address, channelGroup, 4);
    }

    public SConnector connect(UnresolvedAddress address, ChannelGroup channelGroup, final int weight) throws InterruptedException {
        bootstrap.channel(NioSocketChannel.class);
        // 重连
        final ConnectorWatchDog connectorWatchDog = new ConnectorWatchDog(bootstrap, timer, channelGroup) {

            @Override
            public ChannelHandler[] channelHandlers() {
                ChannelHandler extraHandler = extraHandler();
                List<ChannelHandler> handlers = Arrays.asList(
                        this,
                        new LengthFieldPrepender(4),
                        new IdleStateHandler(0, HeartBeats.config().getWriteIdleTime(), 0, HeartBeats.config().getUnit()),
                        new ClientIdleTrigger(),
                        new RemoteTransporterDecoder(),
                        new RemoteTransporterEncoder(),
                        new NettyClientHandler()
                );
                if (extraHandler != null) {
                    handlers = new ArrayList<>(handlers);
                    handlers.add(extraHandler);
                }
                return handlers.toArray(new ChannelHandler[0]);
            }
        };

        SConnector connector = new NettyAbstractConnector(address, channelGroup) {

            @Override
            public void setReConnect(boolean reConnect) {
                // 确定是否需要进行重连，如果消费者与生产者之间，不进行重连，由注册中心控制。
                if (reConnect) {
                    connectorWatchDog.start();
                } else {
                    connectorWatchDog.stop();
                }
            }

            @Override
            public int weight() {
                return weight;
            }
        };
        synchronized (bootstrapLock()) {
            // 处理器
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            .addLast(connectorWatchDog.channelHandlers());
                }
            });
            ChannelFuture channelFuture = bootstrap.connect(InetSocketAddress.createUnresolved(address.getHost(), address.getPort())).sync();
            connector.setChannelFuture(channelFuture);
        }
        return connector;
    }

    @Override
    public void shutdownGracefully() throws InterruptedException {
        if (bootstrap != null) {
            logger.info("client shutdown...");
            // 关闭工作反应器
            worker.shutdownGracefully();
            // 关闭业务处理器
            shutdownProcess();
        }
    }

    protected Bootstrap bootstrapLock() {
        return bootstrap;
    }

    @Override
    protected void processAck(RemoteTransporter remoteTransporter) {

    }

    @Override
    protected ChannelHandler extraHandler() {
        return null;
    }

    class NettyClientHandler extends SimpleChannelInboundHandler<RemoteTransporter> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemoteTransporter msg) throws Exception {
            doRequestAndResponse(ctx, msg);
        }
    }
}
