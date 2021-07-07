package com.jw.screw.provider;

import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.config.RegisterConfig;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.model.MessageNonAck;
import com.jw.screw.common.transport.body.MonitorBody;
import com.jw.screw.common.util.Requires;
import com.jw.screw.provider.annotations.ProviderService;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.SConnector;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务提供者的单播、广播的通知者
 * @author jiangw
 * @date 2020/12/8 14:25
 * @since 1.0
 */
public class Notifier {

    private static Logger logger = LoggerFactory.getLogger(Notifier.class);

    private NettyProvider nettyProvider;

    protected final ExecutorService notifier = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>(), new NamedThreadFactory("notifier"));

    protected final ScheduledExecutorService delayNotifier = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("delay notifier"));

    private final LinkedBlockingQueue<MonitorBody> monitors = new LinkedBlockingQueue<>();

    protected final AtomicBoolean shutdown = new AtomicBoolean(false);

    public Notifier() {
        this(null);
    }

    public Notifier(NettyProvider provider) {
        if (provider != null) {
            this.nettyProvider = provider;
        }
        notifier.submit(new Runnable() {
            @Override
            public void run() {
                while (!shutdown.get()) {
                    try {
                        final MonitorBody monitorBody = monitors.take();
                        if (nettyProvider != null) {
                            RemoteTransporter remoteTransporter = RemoteTransporter.createRemoteTransporter(Protocol.Code.UNICAST, monitorBody);
                            SConnector connector = nettyProvider.getRegistryConnector();
                            Channel channel = connector.createChannel();
                            // 创建Non Ack
                            MessageNonAck messageNonAck = new MessageNonAck();
                            messageNonAck.setUnique(remoteTransporter.getUnique());
                            messageNonAck.setChannel(channel);
                            messageNonAck.setBody(monitorBody);
                            nettyProvider.getNonAck().put(messageNonAck.getUnique(), messageNonAck);
                            channel.writeAndFlush(remoteTransporter);
                        } else {
                            delayNotifier.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    monitors.add(monitorBody);
                                }
                            }, RegisterConfig.delayPublish, RegisterConfig.delayUnit);
                        }
                    } catch (ConnectionException | InterruptedException e) {
                        logger.warn("unicast error: {}", e.getMessage());
                    }
                }
            }
        });
    }

    public void unicast(Object result, Class<?> targetClass, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException, ConnectionException {
        Method[] methods = targetClass.getDeclaredMethods();
        Method method = null;
        for (Method targetMethod : methods) {
            targetMethod.setAccessible(true);
            if (targetMethod.getName().equals(methodName)) {
                method = targetMethod;
                break;
            }
        }
        Requires.isNull(method, "method");
        unicast(result, targetClass, method);
    }

    /**
     * 向注册中心发送单播数据
     * @param result 方法调用的结果
     * @param method 具体调用什么方法
     * @throws ConnectionException
     */
    public void unicast(Object result, Class<?> targetClass, Method method) throws ConnectionException {
        ProviderService publishAnnotation = targetClass.getAnnotation(ProviderService.class);
        Requires.isNull(publishAnnotation, "target class @ProviderService");
        synchronized (this) {
            // 构建单播的body对象
            MonitorBody monitorBody = new MonitorBody();
            NettyProviderConfig providerConfig = nettyProvider.getProviderConfig();
            String providerKey = providerConfig.getServerKey();
            monitorBody.setProviderKey(providerKey);
            monitorBody.setResult(result);
            MonitorBody.MethodWrapper methodWrapper = new MonitorBody.MethodWrapper();
            methodWrapper.setName(method.getName());
            methodWrapper.setParameterTypes(method.getParameterTypes());
            monitorBody.setMethodWrapper(methodWrapper);
            Class<?> clazz = publishAnnotation.publishService();
            monitorBody.setServiceName(clazz.getSimpleName());
            monitors.add(monitorBody);
        }
    }

    public void setNettyProvider(NettyProvider nettyProvider) {
        this.nettyProvider = nettyProvider;
    }

}
