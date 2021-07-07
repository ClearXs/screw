package com.jw.screw.monitor.remote.processor;

import com.jw.screw.monitor.core.MonitorModel;
import com.jw.screw.monitor.core.body.MetricsBody;
import com.jw.screw.monitor.core.mircometer.Metrics;
import com.jw.screw.monitor.remote.MonitorProvider;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.Map;

/**
 * metrics com.jw.screw.monitor.remote.processor
 * @author jiangw
 * @date 2020/12/23 14:28
 * @since 1.0
 */
public class NettyMetricsProcessor implements NettyProcessor {

    private final MonitorProvider monitorProvider;

    public NettyMetricsProcessor(MonitorProvider monitorProvider) {
        this.monitorProvider = monitorProvider;
    }

    @Override
    public RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request) {
        MetricsBody metricsBody = (MetricsBody) request.getBody();
        Map<String, List<Metrics>> metricsMap = metricsBody.getMetricsMap();
        MonitorModel monitorModel = new MonitorModel(metricsBody.getServerKey(), metricsBody.getAddress());
        monitorModel.setRole(metricsBody.getRole());
        monitorProvider.collect(monitorModel, metricsMap);
        return null;
    }
}
