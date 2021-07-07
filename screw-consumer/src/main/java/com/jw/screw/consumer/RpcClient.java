package com.jw.screw.consumer;

import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.monitor.api.ScrewMonitor;
import com.jw.screw.remote.netty.NettyClient;

import java.util.List;

/**
 * 消费者端rpc的客户端
 * @author jiangw
 * @date 2020/12/25 16:20
 * @since 1.0
 */
public class RpcClient extends NettyClient {

    private final com.jw.screw.consumer.NettyConsumerConfig config;

    private ScrewMonitor monitor;

    public RpcClient(com.jw.screw.consumer.NettyConsumerConfig config) {
        super(config.getRpcConfig());
        this.config = config;
    }

    public void setMonitor(final ScrewMonitor monitor, String monitorServerKey) {
        addInboundFilter(new SubscribeFilter(monitorServerKey) {
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

    public com.jw.screw.consumer.NettyConsumerConfig getConfig() {
        return config;
    }
}
