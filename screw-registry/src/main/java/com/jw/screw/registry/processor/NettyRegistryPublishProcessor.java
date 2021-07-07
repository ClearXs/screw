package com.jw.screw.registry.processor;

import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.common.transport.body.AcknowledgeBody;
import com.jw.screw.common.transport.body.PublishBody;
import com.jw.screw.common.util.Collections;
import com.jw.screw.registry.NettyRegistryServer;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 注册中心服务注册的处理器
 * @author jiangw
 * @date 2020/12/8 15:11
 * @since 1.0
 */
public class NettyRegistryPublishProcessor implements NettyProcessor {

    private final NettyRegistryServer registerServer;

    public NettyRegistryPublishProcessor(NettyRegistryServer registerServer) {
        this.registerServer = registerServer;
    }

    @Override
    public RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request) {
        AcknowledgeBody acknowledgeBody = new AcknowledgeBody(request.getUnique(), true);
        PublishBody publishBody = (PublishBody) request.getBody();
        RegisterMetadata registerMetadata = new RegisterMetadata(publishBody.getServiceName(),
                publishBody.getWight(),
                publishBody.getConnCount(),
                publishBody.getPublishAddress());
        List<String> publishServices = publishBody.getPublishServices();
        if (Collections.isNotEmpty(publishServices)) {
            Set<String> registryServices = new HashSet<>(publishServices);
            registerMetadata.setPublishService(registryServices);
        }
        registerServer.handleRegister(ctx.channel(), publishBody.getPublishAddress(), registerMetadata, acknowledgeBody);
        return RemoteTransporter
                .createRemoteTransporter(Protocol.Code.UNKNOWN, acknowledgeBody, request.getUnique(), Protocol.TransportType.ACK);
    }
}
