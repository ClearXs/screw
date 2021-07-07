package com.jw.screw.provider;

import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;

/**
 * 一个provider请求转发器，这个目的就是为了解耦，拓展
 * 比如时rpc调用请求，那么转发到对应rpc处理器中
 * @author jiangw
 * @date 2020/11/27 10:12
 * @since 1.0
 */
public class NettyProviderRequestDispatcher implements NettyProcessor {

    private final NettyProviderRpcRequestProcessor rpcRequestProcessor;


    public NettyProviderRequestDispatcher(NettyProvider provider) {
        this.rpcRequestProcessor = new NettyProviderRpcRequestProcessor(provider);
    }

    @Override
    public RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request) {
        byte code = request.getCode();
        // 处理rpc请求
        if (code == Protocol.Code.RPC_REQUEST) {
            return rpcRequestProcessor.process(ctx, request);
        }
        return null;
    }
}
