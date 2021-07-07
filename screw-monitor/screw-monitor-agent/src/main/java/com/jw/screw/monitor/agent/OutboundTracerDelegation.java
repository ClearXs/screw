package com.jw.screw.monitor.agent;

import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.transport.body.AbstractBody;
import com.jw.screw.common.transport.body.Body;
import com.jw.screw.common.transport.body.RequestBody;
import com.jw.screw.common.util.Collections;
import com.jw.screw.monitor.opentracing.ScrewSpan;
import com.jw.screw.monitor.opentracing.ScrewTracer;
import com.jw.screw.monitor.opentracing.ScrewTracing;
import com.jw.screw.remote.RemoteService;
import com.jw.screw.remote.filter.AbstractFilter;
import com.jw.screw.remote.filter.Filter;
import com.jw.screw.remote.filter.FilterChain;
import com.jw.screw.remote.filter.FilterContext;
import com.jw.screw.remote.modle.RemoteTransporter;
import io.netty.channel.ChannelHandlerContext;

/**
 * outbound filter delegation
 * @author jiangw
 * @date 2020/12/24 11:06
 * @since 1.0
 */
public class OutboundTracerDelegation {

    /**
     * 从byte buddy生成的实例作为委托进行当前方法中
     * @see {@link RemoteService#processRemoteRequest(ChannelHandlerContext, RemoteTransporter)}
     * @param body 远程传输的数据。可以添加{@link AbstractBody#attachment(Object)}
     * @param context {@link FilterContext} 做链路追踪
     * @param chain {@link FilterChain}
     * @param <T> {@link FilterContext}
     */
    public static <T extends FilterContext> void doFilter(Body body, T context, FilterChain chain) throws InterruptedException, RemoteTimeoutException, RemoteSendException {
        // 暂时只针对RequestBody做链路追踪
        if (body instanceof RequestBody) {
            RequestBody requestBody = (RequestBody) body;
            Object attached = requestBody.getAttached();
            if (attached == null) {
                attached = new ScrewTracer();
                requestBody.attachment(attached);
            }
            ScrewTracer tracer = null;
            if (attached instanceof ScrewTracer) {
                tracer = (ScrewTracer) attached;
            }
            if (tracer != null) {
                ScrewSpan span = tracer
                        .buildSpan(requestBody.getServiceName() + StringPool.AT + requestBody.getMethodName())
                        .withTag(ScrewTracing.CODE, context.getTransporter().getCode())
                        .withTag(ScrewTracing.INVOKE_ID, requestBody.getInvokeId())
                        .withTag(ScrewTracing.SERVICE_NAME, requestBody.getServiceName())
                        .withTag(ScrewTracing.METHOD_NAME, requestBody.getMethodName())
                        .withTag(ScrewTracing.PARAMETERS, Collections.toList(requestBody.getParameters()))
                        .withTag(ScrewTracing.RETURN_TYPE, requestBody.getExpectedReturnType())
                        .start();
                tracer.scopeManager().activate(span, true);
            }
        }
        if (chain != null) {
            chain.process(body, context);
        }
    }

    /**
     * @see AbstractFilter#compareTo(Filter)
     */
    public static Integer weight() {
        return Integer.MIN_VALUE;
    }   
}
