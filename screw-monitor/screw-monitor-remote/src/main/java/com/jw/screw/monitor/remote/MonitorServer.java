package com.jw.screw.monitor.remote;

import com.jw.screw.common.model.Tuple;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.NettyServer;
import com.jw.screw.remote.netty.config.NettyServerConfig;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ExecutorService;

/**
 * 收集来自每个调用的者的监控信息
 * @author jiangw
 * @date 2020/12/22 14:19
 * @since 1.0
 */
public class MonitorServer extends NettyServer {

    public MonitorServer(NettyServerConfig serverConfig) {
        super(serverConfig);
    }

    @Override
    public void processRemoteRequest(ChannelHandlerContext ctx, RemoteTransporter request) {
        byte code = request.getCode();
        Tuple<NettyProcessor, ExecutorService> processorTuple = processTables.get(code);
        if (processorTuple == null) {
            return;
        }
        final NettyProcessor processor = processorTuple.getKey();
        ExecutorService executorService = processorTuple.getValue();
        if (code == Protocol.Code.TRACING || code == Protocol.Code.METRICS) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    processor.process(ctx, request);
                }
            });
        }
    }
}
