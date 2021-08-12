package com.jw.screw.spring;

import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.Proxies;
import com.jw.screw.common.SystemConfig;
import com.jw.screw.common.event.Observable;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.exception.RemoteException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.util.Collections;
import com.jw.screw.common.util.Requires;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.consumer.ConnectionWatcher;
import com.jw.screw.consumer.NettyConsumer;
import com.jw.screw.consumer.NettyConsumerConfig;
import com.jw.screw.consumer.model.ProxyObjectFactory;
import com.jw.screw.loadbalance.Rule;
import com.jw.screw.loadbalance.RuleObjectFactory;
import com.jw.screw.common.util.Remotes;
import com.jw.screw.remote.netty.config.GlobeConfig;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import com.jw.screw.spring.anntation.ScrewValue;
import com.jw.screw.spring.config.*;
import com.jw.screw.spring.event.ConfigListener;
import com.jw.screw.spring.event.ConnectionRejectEvent;
import com.jw.screw.spring.event.PropertiesRefreshEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author jiangw
 */
public class ScrewSpringConsumer implements com.jw.screw.spring.ScrewSpring {

    private static Logger logger = LoggerFactory.getLogger(ScrewSpringConsumer.class);

    @ScrewValue("consumer-enable")
    private Boolean enable;

    /**
     * 本身服务的key
     */
    @ScrewValue("consumer-server.key")
    private String serverKey;

    /**
     * 本身服务的port
     */
    @ScrewValue("consumer-provider.server.port")
    private Integer serverPort;

    /**
     * 注册中心host
     */
    @ScrewValue("consumer-registry.address")
    private String registryAddress;

    /**
     * 注册中心端口
     */
    @ScrewValue("consumer-registry.port")
    private Integer registryPort;

    /**
     * 负载均衡
     */
    @ScrewValue("consumer-loadbalance")
    private String loadbalance;

    /**
     * rpc调用延迟等待
     */
    @ScrewValue("consumer-waitMills")
    private Long waitMills;

    /**
     * 配置中心
     */
    @ScrewValue("consumer-config.key")
    private String configServerKey;

    /**
     * 监控中心
     */
    @ScrewValue("consumer-monitor.key")
    private String monitorServerKey;

    /**
     * 监控指标收集周期
     */
    @ScrewValue("consumer-monitor.collect.period")
    private Integer monitorCollectPeriod;

    /**
     * @see com.jw.screw.spring.ConsumerWrapper
     */
    private com.jw.screw.spring.ConsumerWrapper consumerWrapper;

    /**
     * @see com.jw.screw.spring.ProxyHandler
     */
    private Map<Class<?>, com.jw.screw.spring.ProxyHandler> proxies;

    /**
     * @see NettyConsumer
     */
    private NettyConsumer nettyConsumer;

    /**
     * @see ApplicationContext
     */
    private ApplicationContext applicationContext;

    /**
     * @see NettyConsumerConfig
     */
    private NettyConsumerConfig consumerConfig;

    private final ExecutorService consumer = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            new NamedThreadFactory("screw consumer"));

    @Override
    public void initConfig() throws RemoteException, IOException {
        NettyClientConfig registryConfig = new NettyClientConfig();
        UnresolvedAddress remoteAddress = new RemoteAddress(registryAddress, registryPort);
        // 注册中心
        registryConfig.setDefaultAddress(remoteAddress);
        // 负载均衡
        Rule rule = RuleObjectFactory.getRuleByName(loadbalance);
        consumerConfig.setRule(rule);
        consumerConfig.setRegistryConfig(registryConfig);

        // 服务本身信息
        consumerConfig.setServerKey(serverKey);
        consumerConfig.setPort(serverPort);
        consumerConfig.setRole(SystemConfig.CONSUMER.getRole());
        // 配置中心
        consumerConfig.setConfigServerKey(configServerKey);
        // 监控中心
        consumerConfig.setMonitorServerKey(monitorServerKey);
        consumerConfig.setMonitorCollectPeriod(monitorCollectPeriod);
    }

    @Override
    public void validateParams() {
        // 验证参数
        Requires.isNull(registryAddress, "registryAddress");
        Requires.isNull(registryPort, "registryPort");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!enable) {
            return;
        }
        validateParams();
        // 检测注册中心是否可连接
        boolean connectable = Remotes.connectable(registryAddress, registryPort, GlobeConfig.CONNECT_TIMEOUT_MILLIS);
        if (!connectable) {
            ConfigListener.rejectConnection(applicationContext);
            applicationContext.publishEvent(new PropertiesRefreshEvent(applicationContext));
            ValueRefreshContext refreshContext = applicationContext.getBean(ValueRefreshContext.class);
            refreshContext.onPut(applicationContext);
        }
        final CountDownLatch lock = new CountDownLatch(1);
        AtomicBoolean isConnectionConfigCenter = new AtomicBoolean(false);
        // 是否可连接
        AtomicBoolean isConnection = new AtomicBoolean(false);
        // 初次连接 -> 连接可用，执行start，执行外部外码快，此时连接配置中心（不应该在初次连接在内部代码块中连接）
        // 初次连接 -> 连接不可用，执行外部代码块，不执行外部代码块
        // 再次连接 -> 连接可用，如果不是断线重连，那么此时还未连接配置中心
        // 再次连接 -> 连接可用，如果是断线重连，此时不应该做什么操作
        consumer.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    consumerConfig = new NettyConsumerConfig();
                    // 初始化配置
                    initConfig();
                    nettyConsumer = new NettyConsumer(consumerConfig);
                    ConnectionListener connectionListener = new ConnectionListener() {

                        @Override
                        protected void accept(Observable observable, Object obj) {
                            isConnection.set(true);
                            lock.countDown();
                        }

                        @Override
                        protected void reject(Observable observable, Exception e) {
                            applicationContext.publishEvent(new ConnectionRejectEvent(applicationContext));
                            lock.countDown();
                            isConnection.set(false);
                        }
                    };
                    nettyConsumer.start(connectionListener);
                    if (StringUtils.isNotEmpty(configServerKey) && !isConnectionConfigCenter.get()) {
                        // 配置中心
                        try {
                            ConfigCenterConnectable
                                    .getInstance(applicationContext)
                                    .configCenter(configServerKey, nettyConsumer);
                            ValueRefreshContext refreshContext = applicationContext.getBean(ValueRefreshContext.class);
                            refreshContext.onPut(applicationContext);
                        } catch (RemoteTimeoutException | ConnectionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        lock.await();
        if (StringUtils.isNotEmpty(configServerKey) && isConnection.get()) {
            // 配置中心
            try {
                isConnectionConfigCenter.set(true);
                ConfigCenterConnectable
                        .getInstance(applicationContext)
                        .configCenter(configServerKey, nettyConsumer);
                ValueRefreshContext refreshContext = applicationContext.getBean(ValueRefreshContext.class);
                refreshContext.onPut(applicationContext);
            } catch (RemoteTimeoutException | ConnectionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        generateMethodProxies();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Environment environment = applicationContext.getEnvironment();
        if (environment instanceof ConfigurableEnvironment) {
            PropertySource<?> propertySource = ((ConfigurableEnvironment) environment).getPropertySources().get("consumer");
            if (propertySource != null) {
                Property.addResource(new TypePropertySource<>(propertySource));
                Property.refreshProperties(this);
            }
        }
        this.applicationContext = applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        if (nettyConsumer != null) {
            nettyConsumer.stop();
        }
    }

    /**
     * 根据服务包装信息，生成服务的代理对象
     */
    private void generateMethodProxies() {
        if (consumerWrapper == null) {
            return;
        }
        List<com.jw.screw.spring.ConsumerWrapper.ServiceWrapper> serviceWrappers = consumerWrapper.getServiceWrappers();
        if (Collections.isEmpty(serviceWrappers)) {
            return;
        }
        for (com.jw.screw.spring.ConsumerWrapper.ServiceWrapper serviceWrapper : serviceWrappers) {
            final String serverKey = serviceWrapper.getServerKey();
            final boolean isAsync = Proxies.parseInvokeType(serviceWrapper.getInvokeType());
            List<Class<?>> services = serviceWrapper.getServices();
            if (Collections.isEmpty(services)) {
                return;
            }
            com.jw.screw.spring.ProxyHandler proxyHandler = new com.jw.screw.spring.ProxyHandler() {

                @Override
                protected <T> T handle(Class<T> proxyClass) {
                    try {
                        ServiceMetadata serviceMetadata = new ServiceMetadata(serverKey);
                        ConnectionWatcher connectionWatcher = nettyConsumer.watchConnect(serviceMetadata);
                        // 连接等待
                        boolean isConnected = connectionWatcher.waitForAvailable(waitMills);
                        if (!isConnected) {
                            return null;
                        }
                        return ProxyObjectFactory
                                .factory()
                                .isAsync(isAsync)
                                .consumer(nettyConsumer)
                                .metadata(serviceMetadata)
                                .connectWatch(connectionWatcher)
                                .newProxyInstance(proxyClass);
                    } catch (InterruptedException | ConnectionException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            proxies = new HashMap<>(services.size());
            for (Class<?> service : services) {
                proxies.put(service, proxyHandler);
            }
        }
    }

    public NettyConsumer getConsumer() {
        return nettyConsumer;
    }

    public com.jw.screw.spring.ConsumerWrapper getConsumerWrapper() {
        return consumerWrapper;
    }

    public Map<Class<?>, com.jw.screw.spring.ProxyHandler> getProxies() {
        return proxies;
    }

    public void setConsumerWrapper(com.jw.screw.spring.ConsumerWrapper consumerWrapper) {
        this.consumerWrapper = consumerWrapper;
    }

    static class ConsumerBean<T> implements FactoryBean<T> {

        private final Class<T> clazz;

        private final com.jw.screw.spring.ProxyHandler handler;

        public ConsumerBean(Class<T> clazz, com.jw.screw.spring.ProxyHandler handler) {
            this.clazz = clazz;
            this.handler = handler;
        }

        @Override
        public T getObject() throws Exception {
            return handler.handle(clazz);
        }

        @Override
        public Class<T> getObjectType() {
            return clazz;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }
    }
}
