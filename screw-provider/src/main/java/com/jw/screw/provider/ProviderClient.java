package com.jw.screw.provider;

import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.common.transport.body.AcknowledgeBody;
import com.jw.screw.common.transport.body.RegisterBody;
import com.jw.screw.common.util.Collections;
import com.jw.screw.monitor.api.ScrewMonitor;
import com.jw.screw.provider.model.ServiceWrapper;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.NettyClient;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.List;
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

    private ScrewMonitor monitor;

    private final NettyProviderConfig config;

    /**
     * 如果是第一次连接，那么一定会进入该方法，在现有的判断下，需要加上重连次数。
     * 以0作为标识，若当前值为0，那么就是初始化，否则就是断线重连
     */
    private final AtomicInteger counter = new AtomicInteger(0);

    public ProviderClient(NettyProviderConfig config, NettyProvider nettyProvider) {
        super(config.getRegisterConfig());
        this.nettyProvider = nettyProvider;
        this.config = config;
    }

    @Override
    public void processRemoteResponse(ChannelHandlerContext ctx, RemoteTransporter remoteTransporter) {
        byte code = remoteTransporter.getCode();
        if (code == Protocol.Code.MONITOR_ADDRESS) {
            RegisterBody body = (RegisterBody) remoteTransporter.getBody();
            List<RegisterMetadata> registerMetadata = body.getRegisterMetadata();
            if (monitor != null && Collections.isNotEmpty(registerMetadata)) {
                monitor.setMonitorAddress(registerMetadata.toArray(new RegisterMetadata[] {}));
            }
        } else {
            super.processRemoteResponse(ctx, remoteTransporter);
        }
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

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                if (counter.getAndIncrement() != 0) {
                    nettyProvider.getServiceWrapperManager().register(nettyProvider.getPublishCached().toArray());
                    LinkedBlockingQueue<ServiceWrapper> wrappers = nettyProvider.getWrappers();
                    // 判断去除相同对象，addAll的目的是wrapper被消费了(???)
                    if (wrappers != null && wrappers != nettyProvider.getServiceWrapperManager().wrapperContainer().wrappers()) {
                        wrappers.addAll(nettyProvider.getServiceWrapperManager().wrapperContainer().wrappers());
                    }
                }
            }
        };
    }

    public void setMonitor(final ScrewMonitor monitor) {
        addInboundFilter(new SubscribeFilter(config.getMonitorServerKey()) {
            @Override
            protected void handle(List<RegisterMetadata> registerMetadata) {
                monitor.setMonitorAddress(registerMetadata.toArray(new RegisterMetadata[]{}));
            }
        });
        this.monitor = monitor;
    }

    public ScrewMonitor getMonitor() {
        return monitor;
    }

    public NettyProviderConfig getConfig() {
        return config;
    }
}
