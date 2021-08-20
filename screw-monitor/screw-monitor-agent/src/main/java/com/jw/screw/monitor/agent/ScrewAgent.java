package com.jw.screw.monitor.agent;

import com.jw.screw.common.Status;
import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.proxy.ByteBuddyInvocationInterceptor;
import com.jw.screw.common.proxy.ProxyFactory;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.transport.body.AbstractBody;
import com.jw.screw.common.transport.body.Body;
import com.jw.screw.common.transport.body.RequestBody;
import com.jw.screw.common.transport.body.ResponseBody;
import com.jw.screw.common.util.Collections;
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
import io.netty.channel.ChannelHandlerContext;
import io.opentracing.log.Fields;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.Super;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * <b>screw agent</b>
 * <p>1.动态创建TracingFilter-Class</p>
 * <p>2.拦截remote的inboundFilter与outboundFilter动态添加filter</p>
 * <p>3.filter中做链路记录，并入monitor-client的缓存中，向monitor-server发送数据</p>
 * 注：一个请求的发送一定是服务提供者收到，提供者处理请求，消费者接受响应，发送给监控中心。所以，要实现tracer的传递一定是在提供者处。
 *    此外一个服务一定是消费者才拥有链路追踪的信息，所以一个服务要拥有链路消息一定是消费者处
 * @author jiangw
 * @date 2020/12/23 17:08
 * @since 1.0
 */
public class ScrewAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScrewAgent.class);

    private final static String INBOUND_MONITOR_FILTER = "InboundMonitorFilter";

    private final static String OUTBOUND_MONITOR_FILTER = "OutboundMonitorFilter";

    /**
     * 入站监控的处理器
     */
    private static Filter inboundMonitorFilter;

    /**
     * 出站监控处理器
     */
    private static Filter outboundMonitorFilter;

    public static void premain(String arg, Instrumentation instrumentation) {

        inboundMonitorFilter = ProxyFactory.proxy()
                .withName(ScrewAgent.class.getPackage().getName().concat(StringPool.DOT) + INBOUND_MONITOR_FILTER)
                .newProxyInstance(AbstractFilter.class, new InboundTracerDelegation(), true);

        outboundMonitorFilter = ProxyFactory.proxy()
                .withName(ScrewAgent.class.getPackage().getName().concat(StringPool.DOT) + OUTBOUND_MONITOR_FILTER)
                .newProxyInstance(AbstractFilter.class, new OutboundTracerDelegation(), true);
        AgentBuilder.Default builder = new AgentBuilder.Default();

        AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {

            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
                return builder.method(ElementMatchers.named("init")).intercept(MethodDelegation.to(AgentInterceptor.class));
            }
        };

        builder
            .type(ElementMatchers.nameStartsWith("com.jw.screw.remote.netty"))
            .transform(transformer)
            .with(new AgentListener())
            .installOn(instrumentation);
    }

    /**
     * byte buddy - delegation
     */
    public static class AgentInterceptor {

        @RuntimeType
        public static Object intercept(@SuperCall Callable<?> callable, @Super RemoteService remoteService) {
            try {
                remoteService.addInboundFilter(inboundMonitorFilter);
                remoteService.addOutboundFilter(outboundMonitorFilter);
                return callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * inbound filter delegation
     * @author jiangw
     * @date 2020/12/24 11:01
     * @since 1.0
     * @modifyDate 2021/08/13 17:10
     */
    public static class InboundTracerDelegation implements ByteBuddyInvocationInterceptor {

        private final static String DO_FILTER = "doFilter";

        private final static String WEIGHT = "weight";

        @Override
        public Object invoke(Object proxy, Method method, Object[] args, Callable<?> callable) {
            String name = method.getName();
            if (DO_FILTER.equals(name)) {
                if (args.length == 3) {
                    Body body = (Body) args[0];
                    FilterContext filterContext = (FilterContext) args[1];
                    FilterChain filterChain = (FilterChain) args[2];
                    try {
                        doFilter(body, filterContext, filterChain);
                    } catch (InterruptedException | RemoteTimeoutException | RemoteSendException | ConnectionException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (WEIGHT.equals(name)) {
                return weight();
            }
            try {
                return callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 从byte buddy生成的实例作为委托进行当前方法中
         * @param body 远程传输的数据。可以添加{@link AbstractBody#attachment(Object)}
         * @param context {@link FilterContext} 做链路追踪
         * @param chain {@link FilterChain}
         * @param <T> {@link FilterContext}
         */
        public <T extends FilterContext> void doFilter(Body body, T context, FilterChain chain) throws InterruptedException, RemoteTimeoutException, RemoteSendException, ConnectionException {
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
                                Map<String, Object> fields = new HashMap<>(16);
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

        /**
         * @see AbstractFilter#compareTo(Filter)
         */
        public Integer weight() {
            return Integer.MIN_VALUE;
        }

        private <T extends FilterContext> void sendTrace(ScrewSpan span, T context) throws ConnectionException {
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
    }

    /**
     * outbound filter delegation
     * @author jiangw
     * @date 2020/12/24 11:06
     * @since 1.0
     * @modifyDate 2021/08/13 17:10
     */
    public static class OutboundTracerDelegation implements ByteBuddyInvocationInterceptor {

        private final static String DO_FILTER = "doFilter";

        private final static String WEIGHT = "weight";

        @Override
        public Object invoke(Object proxy, Method method, Object[] args, Callable<?> callable) {
            String name = method.getName();
            if (DO_FILTER.equals(name)) {
                if (args.length == 3) {
                    Body body = (Body) args[0];
                    FilterContext filterContext = (FilterContext) args[1];
                    FilterChain filterChain = (FilterChain) args[2];
                    try {
                        doFilter(body, filterContext, filterChain);
                    } catch (InterruptedException | RemoteTimeoutException | RemoteSendException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (WEIGHT.equals(name)) {
                return weight();
            }
            try {
                return callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

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

    public static class AgentListener implements AgentBuilder.Listener {

        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
        }

        @Override
        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {

        }

        @Override
        public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {

        }

        @Override
        public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
            LOGGER.info("on error: {}", typeName);
        }

        @Override
        public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

        }
    }
}
