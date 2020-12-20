package com.jw.screw.registry.processor;

import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.transport.body.RegisterBody;
import com.jw.screw.common.transport.body.SubscribeBody;
import com.jw.screw.registry.NettyRegistryServer;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * 注册中心对于消费者的订阅请求的处理器
 * @author jiangw
 * @date 2020/12/8 15:10
 * @since 1.0
 */
public class NettyRegistrySubscribeProcessor implements NettyProcessor {

    private final NettyRegistryServer registryServer;

    public NettyRegistrySubscribeProcessor(NettyRegistryServer registryServer) {
        this.registryServer = registryServer;
    }

    @Override
    public RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request) {
        SubscribeBody subscribeBody = (SubscribeBody) request.getBody();
        ServiceMetadata serviceMetadata = subscribeBody.getServiceMetadata();

        List<RegisterMetadata> registerMetadata = registryServer.handleSubscribe(serviceMetadata);
        RegisterBody registerBody = new RegisterBody(serviceMetadata.getServiceProviderName(), registerMetadata);
        return RemoteTransporter.createRemoteTransporter(Protocol.Code.RESPONSE_SUBSCRIBE, registerBody, request.getUnique());
    }
}
