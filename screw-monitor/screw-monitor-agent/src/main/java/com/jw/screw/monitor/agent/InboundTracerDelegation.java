package com.jw.screw.monitor.agent;

import com.jw.screw.common.Status;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.transport.body.AbstractBody;
import com.jw.screw.common.transport.body.Body;
import com.jw.screw.common.transport.body.ResponseBody;
import com.jw.screw.consumer.RpcClient;
import com.jw.screw.loadbalance.BaseConfig;
import com.jw.screw.monitor.api.ScrewMonitor;
import com.jw.screw.monitor.opentracing.*;
import com.jw.screw.provider.ProviderClient;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.RemoteService;
import com.jw.screw.remote.filter.AbstractFilter;
import com.jw.screw.remote.filter.Filter;
import com.jw.screw.remote.filter.FilterChain;
import com.jw.screw.remote.filter.FilterContext;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.SConnector;
import io.netty.channel.Channel;
import io.opentracing.log.Fields;

import java.util.HashMap;
import java.util.Map;

/**
 * inbound filter delegation
 * @author jiangw
 * @date 2020/12/24 11:01
 * @since 1.0
 */
public class InboundTracerDelegation {

    /**
     * 从byte buddy生成的实例作为委托进行当前方法中
     * @param body 远程传输的数据。可以添加{@link AbstractBody#attachment(Object)}
     * @param context {@link FilterContext} 做链路追踪
     * @param chain {@link FilterChain}
     * @param <T> {@link FilterContext}
     */
    public static <T extends FilterContext> void doFilter(Body body, T context, FilterChain chain) throws InterruptedException, RemoteTimeoutException, RemoteSendException, ConnectionException {
        // 暂时只考虑ResponseBody
        if (body instanceof ResponseBody) {
            ResponseBody responseBody = (ResponseBody) body;
            Object attached = responseBody.getAttached();
            if (attached instanceof ScrewTracer) {
                ScrewTracer tracer = (ScrewTracer) attached;
                ScrewSpan span = (ScrewSpan) tracer.activeSpan();
                if (span != null) {
                    long invokeId = (long) span.getTag(ScrewTracing.INVOKE_ID);
                    if (responseBody.getInvokeId() == invokeId) {
                        byte status = responseBody.getStatus();
                        span.setTag(ScrewTracing.STATUS, status);
                        span.setTag(ScrewTracing.RESULT, responseBody.getResult());
                        span.setTag(ScrewTracing.ERROR, responseBody.getError());
                        if (status == Status.OK.getValue()) {
                            span.log(Fields.MESSAGE);
                        } else if (status == Status.SERVICE_INVOKE_ERROR.getValue()) {
                            Map<String, Object> fields = new HashMap<>();
                            fields.put(Fields.ERROR_OBJECT, Status.SERVICE_INVOKE_ERROR.getDescription());
                            fields.put(Fields.STACK, responseBody.getExceptionTrace());
                            span.log(fields);
                        }
                    }
                    ScrewScopeManager scopeManager = (ScrewScopeManager) tracer.scopeManager();
                    scopeManager.close();
                    sendTrace(span, context);
                }
            }
        }
        if (chain != null) {
            chain.process(body, context);
        }
    }

    private static <T extends FilterContext> void sendTrace(ScrewSpan span, T context) throws ConnectionException {
        // 构建monitor trace
        RemoteService remoteService = context.getRemoteService();
        ScrewMonitor monitor = null;
        BaseConfig config = null;
        if (remoteService instanceof RpcClient) {
            RpcClient rpcClient = (RpcClient) remoteService;
            monitor = rpcClient.getMonitor();
            config = rpcClient.getConfig();
        }
        if (remoteService instanceof ProviderClient) {
            ProviderClient providerClient = (ProviderClient) remoteService;
            monitor = providerClient.getMonitor();
            config = providerClient.getConfig();
        }
        if (monitor != null && config != null) {
            TraceBody traceBody = new TraceBody();
            UnresolvedAddress serverAddress = new RemoteAddress(config.getServerHost(), config.getPort());
            String serverKey = config.getServerKey();
            traceBody.setScrewSpan(span);
            traceBody.setServerKey(serverKey);
            traceBody.setServerAddress(serverAddress);
            RemoteTransporter transporter = RemoteTransporter.createRemoteTransporter(Protocol.Code.TRACING, traceBody);
            SConnector connector = monitor.getConnector();
            if (connector == null) {
                throw new IllegalArgumentException("load balance error, maybe service isn't useful.");
            }
            Channel channel = connector.createChannel();
            channel.writeAndFlush(transporter);
        }
    }

    /**
     * @see AbstractFilter#compareTo(Filter)
     */
    public static Integer weight() {
        return Integer.MIN_VALUE;
    }
}
