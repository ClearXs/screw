package com.jw.screw.monitor.remote.processor;

import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.util.Collections;
import com.jw.screw.monitor.opentracing.ScrewSpan;
import com.jw.screw.monitor.opentracing.ScrewSpanContext;
import com.jw.screw.monitor.opentracing.ScrewSpanReferences;
import com.jw.screw.monitor.opentracing.TraceBody;
import com.jw.screw.monitor.remote.MonitorProvider;
import com.jw.screw.monitor.remote.TracingModel;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.opentracing.References;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * tracing com.jw.screw.monitor.remote.processor
 * 1.一个span的发送是由不同服务发送，在monitor中只记录某个服务的链路（它可能处于链路的root，也可能处于child）实例数据。
 * 2.如果处于child span，那么他的reference一定记录了关于parent span信息，以此构建调用链
 * 3.{@link TracingModel#getTracers()}记录了某个调用链路的root span信息
 * 4.基于{@link MonitorProvider#getClutterSpans()}构建调用链，一个链中child一定是最先添加进clutter span中，parent则之后，所以可以通过
 * {@link ScrewSpan#getReferences()}进行构建，只要traceId与spanId 相同那么就可以把clutter span放入parent span中
 * <b>问题：</b>
 *      1.针对一条调用链路，是否存在一个服务接受多个span的情况：可能存在，针对那些平级的span
 * @author jiangw
 * @date 2020/12/23 17:00
 * @since 1.0
 */
public class NettyTracingProcessor implements NettyProcessor {

    private final MonitorProvider monitorProvider;

    public NettyTracingProcessor(MonitorProvider monitorProvider) {
        this.monitorProvider = monitorProvider;
    }

    @Override
    public RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request) {
        TraceBody traceBody = (TraceBody) request.getBody();
        String serverKey = traceBody.getServerKey();
        UnresolvedAddress serverAddress = traceBody.getServerAddress();
        ScrewSpan remoteSpan = traceBody.getScrewSpan();
        String traceKey = serverKey + StringPool.AT + serverAddress.getHost() + StringPool.AT + serverAddress.getPort();
        String tracerId = remoteSpan.context().getTracerId();
        ConcurrentHashMap<String, TracingModel> serverTracing = monitorProvider.getServerTracing();
        // 查找某个server && address的链路信息，这样可以实现某个server（消费者）的链路信息
        TracingModel tracingModel = serverTracing.get(traceKey);
        if (tracingModel == null) {
            tracingModel = new TracingModel(serverKey, serverAddress);
            serverTracing.put(traceKey, tracingModel);
        }
        // 获取某个trace的追踪链
        Map<String, ScrewSpan> tracers = tracingModel.getTracers();
        // 获取当前root span，root span的存在只有在那些处于平级的span。
        ScrewSpan rootSpan = tracers.computeIfAbsent(tracerId, k -> remoteSpan);
        List<ScrewSpan> clutterSpans = monitorProvider.getClutterSpans();
        // 如果root span与 remote span那么两者就是兄弟span
        if (!rootSpan.equals(remoteSpan)) {
            rootSpan.siblingSpan(remoteSpan);
        }
        parseSpanReference(rootSpan, remoteSpan, clutterSpans);
        // 添加进clutter span
        clutterSpans.add(remoteSpan);
        return null;
    }

    /**
     * 基于clutterSpans构建子span，如果root span与remote span一致，责任没有平级span
     * @param rootSpan 当前服务root span
     * @param clutterSpans 从中找到与root
     */
    public void parseSpanReference(ScrewSpan rootSpan, ScrewSpan remoteSpan, List<ScrewSpan> clutterSpans) {
        // 1.如果root span 与 remote span一致，那么可以认为当前服务的调用链路中，还未存在平级的span
        List<ScrewSpan> sameTraceIdSpan = clutterSpans.stream()
                .filter(o -> o.context().getTracerId().equals(rootSpan.context().getTracerId()))
                .collect(Collectors.toList());
        if (Collections.isEmpty(sameTraceIdSpan)) {
            return;
        }
        // 2.构建root的child
        for (ScrewSpan span : sameTraceIdSpan) {
            List<ScrewSpanReferences> references = span.getReferences();
            if (Collections.isEmpty(references)) {
                return;
            }
            // child_of在reference一定存在一个
            for (ScrewSpanReferences reference : references) {
                String referenceType = reference.getType();
                if (References.CHILD_OF.equals(referenceType)) {
                    ScrewSpanContext spanContext = reference.getSpanContext();
                    // 当trace id、span id、baggage相同，则添加当前span
                    if (rootSpan.equals(spanContext)) {
                        rootSpan.childSpan(span);
                    }
                } else if (References.FOLLOWS_FROM.equals(referenceType)) {
                    // 当trace id相同，则添加为follow span
                    if (reference.getSpanContext().getTracerId().equals(rootSpan.context().getTracerId())) {
                        rootSpan.followSpan(span);
                    }
                }
            }
        }
    }
}
