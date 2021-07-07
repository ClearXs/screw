package com.jw.screw.monitor.agent;

import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.transport.body.Body;
import com.jw.screw.remote.RemoteService;
import com.jw.screw.remote.filter.AbstractFilter;
import com.jw.screw.remote.filter.Filter;
import com.jw.screw.remote.filter.FilterChain;
import com.jw.screw.remote.filter.FilterContext;
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
import java.lang.reflect.Modifier;
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
        try {
            inboundMonitorFilter = new ByteBuddy()
                    .subclass(AbstractFilter.class)
                    .name(ScrewAgent.class.getPackage().getName().concat(StringPool.DOT) + INBOUND_MONITOR_FILTER)
                    .defineMethod("doFilter", void.class, Modifier.PUBLIC)
                    .withParameters(Body.class, FilterContext.class, FilterChain.class)
                    .intercept(MethodDelegation.to(com.jw.screw.monitor.agent.InboundTracerDelegation.class))
                    .defineMethod("weight", Integer.class, Modifier.PUBLIC)
                    .intercept(MethodDelegation.to(com.jw.screw.monitor.agent.InboundTracerDelegation.class))
                    .make()
                    .load(ClassLoader.getSystemClassLoader())
                    .getLoaded()
                    .newInstance();

            outboundMonitorFilter = new ByteBuddy()
                    .subclass(AbstractFilter.class)
                    .name(ScrewAgent.class.getPackage().getName().concat(StringPool.DOT) + OUTBOUND_MONITOR_FILTER)
                    .defineMethod("doFilter", void.class, Modifier.PUBLIC)
                    .withParameters(Body.class, FilterContext.class, FilterChain.class)
                    .intercept(MethodDelegation.to(com.jw.screw.monitor.agent.OutboundTracerDelegation.class))
                    .defineMethod("weight", Integer.class, Modifier.PUBLIC)
                    .intercept(MethodDelegation.to(com.jw.screw.monitor.agent.OutboundTracerDelegation.class))
                    .make()
                    .load(ClassLoader.getSystemClassLoader())
                    .getLoaded()
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("create boundTracerDelegation failed: {}", e.getMessage());
            return;
        }

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
