package com.jw.screw.registry;

import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.config.RegisterConfig;
import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.registry.processor.NettyRegistryPublishProcessor;
import com.jw.screw.registry.processor.NettyRegistrySubscribeProcessor;
import com.jw.screw.registry.processor.NettyRegistryUnicastProcessor;
import com.jw.screw.remote.NonAckScanner;
import com.jw.screw.remote.Protocol;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.*;

/**
 * @author jiangw
 * @date 2020/12/8 11:49
 * @since 1.0
 */
public class DefaultRegistry extends AbstractRegistry {

    private static Logger logger = LoggerFactory.getLogger(DefaultRegistry.class);

    /**
     * 服务注册的线程池
     */
    private ExecutorService registerExecutors;

    /**
     * 服务订阅的线程池
     */
    private ExecutorService subscribeExecutors;

    /**
     * 服务单播的线程池
     */
    private ExecutorService unicastExecutors;

    public DefaultRegistry(int port) {
        super(port);
        init();
    }

    private void init() {
        registerExecutors = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("registry register"));
        subscribeExecutors = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("registry subscribe"));
        unicastExecutors = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("registry unicast"));
        // 服务注册处理器
        registerServer.registerProcessors(Protocol.Code.SERVICE_REGISTER, new NettyRegistryPublishProcessor(registerServer), registerExecutors);
        // 订阅服务的处理器
        registerServer.registerProcessors(Protocol.Code.SERVICE_SUBSCRIBE, new NettyRegistrySubscribeProcessor(registerServer), subscribeExecutors);
        // 服务单播处理器
        registerServer.registerProcessors(Protocol.Code.UNICAST, new NettyRegistryUnicastProcessor(registerServer), unicastExecutors);
    }

    @Override
    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("----- stated register -----");
        }
        registerServer.start();
        try {
            synchronized (registryContext.getGlobalRegisterInfo()) {
                Channel channel = registerServer.channel();
                UnresolvedAddress unresolvedAddress = new RemoteAddress(InetAddress.getLocalHost().getHostAddress(), registryConfig.getListenerPort());
                RegisterMetadata registerMetadata = new RegisterMetadata("registry server", unresolvedAddress);
                registerServer.registerService(channel, unresolvedAddress, registerMetadata);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();

        registerExecutors.shutdown();
        subscribeExecutors.shutdown();
        unicastExecutors.shutdown();
    }

    {
        // 对那些没有获取响应的Ack，定时发送
        ScheduledThreadPoolExecutor ackScheduler = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("registry ack scheduler", true));
        ackScheduler.scheduleAtFixedRate(new NonAckScanner(registryContext.getNonAck()),
                RegisterConfig.delayPublish, RegisterConfig.delayPeriod, RegisterConfig.delayUnit);
    }
}
