package com.jw.screw.provider;

import com.jw.screw.common.transport.body.AcknowledgeBody;
import com.jw.screw.provider.model.ServiceWrapper;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.NettyClient;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * screw
 * @author jiangw
 * @date 2020/12/9 9:30
 * @since 1.0
 */
public class ProviderClient extends NettyClient {

    private final NettyProvider nettyProvider;

    public ProviderClient(NettyClientConfig clientConfig, NettyProvider nettyProvider) {
        super(clientConfig);
        this.nettyProvider = nettyProvider;
    }

    @Override
    protected void processAck(RemoteTransporter remoteTransporter) {
        if (remoteTransporter.getTransporterType() == Protocol.TransportType.ACK) {
            AcknowledgeBody acknowledgeBody = (AcknowledgeBody) remoteTransporter.getBody();
            nettyProvider.getNonAck().remove(acknowledgeBody.getSequence());
        }
    }

    @Override
    protected ChannelHandler extraHandler() {
        // 与注册中心重连成功，重新发布服务
        return new ChannelInboundHandlerAdapter() {

            /**
             * 如果是第一次连接，那么一定会进入该方法，在现有的判断下，需要加上重连次数。
             */
            private final ThreadLocal<AtomicInteger> counter = new ThreadLocal<>();

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                nettyProvider.getServiceWrapperManager().register(nettyProvider.getPublishCached());
                LinkedBlockingQueue<ServiceWrapper> wrappers = nettyProvider.getWrappers();
                AtomicInteger reConnectCounter = counter.get();
                counter.remove();
                if (reConnectCounter == null) {
                    reConnectCounter = new AtomicInteger(0);
                }
                if (reConnectCounter.getAndIncrement() != 0) {
                    if (wrappers != null) {
                        wrappers.addAll(nettyProvider.getServiceWrapperManager().wrapperContainer().wrappers());
                    }
                }
                counter.set(reConnectCounter);
            }
        };
    }
}
