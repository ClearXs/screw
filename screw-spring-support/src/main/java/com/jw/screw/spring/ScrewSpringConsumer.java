package com.jw.screw.spring;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jw.screw.common.Proxies;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.exception.RemoteException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.future.FutureListener;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.util.Collections;
import com.jw.screw.common.util.Requires;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.consumer.ConnectWatch;
import com.jw.screw.consumer.NettyConsumer;
import com.jw.screw.consumer.NettyConsumerConfig;
import com.jw.screw.consumer.RepeatableFuture;
import com.jw.screw.consumer.model.ProxyObjectFactory;
import com.jw.screw.loadbalance.Rule;
import com.jw.screw.loadbalance.RuleObjectFactory;
import com.jw.screw.remote.Remotes;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import com.jw.screw.spring.anntation.ScrewValue;
import com.jw.screw.spring.event.PropertiesRefreshEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.*;
import org.springframework.core.io.support.ResourcePropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author jiangw
 */
public class ScrewSpringConsumer implements InitializingBean, ApplicationContextAware,  DisposableBean {

    private static Logger logger = LoggerFactory.getLogger(ScrewSpringConsumer.class);

    @ScrewValue("consumer-registry.address")
    private String registryAddress;

    @ScrewValue("consumer-registry.port")
    private Integer registryPort;

    @ScrewValue("consumer-consumer.loadbalance")
    private String loadbalance;

    @ScrewValue("consumer-consumer.waitMills")
    private Long waitMills;

    @ScrewValue("consumer-consumer.key")
    private String consumerKey;

    @ScrewValue("consumer-config.key")
    private String configKey;

    private ConsumerWrapper consumerWrapper;

    private Map<Class<?>, ProxyHandler> proxies;

    private boolean goon = false;

    private NettyConsumer nettyConsumer;

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!goon) {
            return;
        }
        // 验证参数
        Requires.isNull(registryAddress, "registryAddress");
        Requires.isNull(registryPort, "registryPort");
        NettyConsumerConfig consumerConfig = new NettyConsumerConfig();
        NettyClientConfig registryConfig = new NettyClientConfig();
        // 判断注册中心是否可连接
        boolean connectable = Remotes.connectable(registryAddress, registryPort, 300000);
        if (!connectable) {
            throw new RemoteException("registry can't connect");
        }
        UnresolvedAddress remoteAddress = new RemoteAddress(registryAddress, registryPort);
        // 注册中心地址
        registryConfig.setDefaultAddress(remoteAddress);
        Rule rule = RuleObjectFactory.getRuleByName(loadbalance);
        consumerConfig.setRule(rule);
        consumerConfig.setRegistryConfig(registryConfig);
        nettyConsumer = new NettyConsumer(consumerConfig);
        nettyConsumer.start();
        // 默认连接配置中心
        configCenter();
        List<ConsumerWrapper.ServiceWrapper> serviceWrappers = consumerWrapper.getServiceWrappers();
        if (Collections.isEmpty(serviceWrappers)) {
            return;
        }
        for (ConsumerWrapper.ServiceWrapper serviceWrapper : serviceWrappers) {
            final String serverKey = serviceWrapper.getServerKey();
            final boolean isAsync = Proxies.parseInvokeType(serviceWrapper.getInvokeType());
            List<Class<?>> services = serviceWrapper.getServices();
            if (Collections.isEmpty(services)) {
                return;
            }
            ProxyHandler proxyHandler = new ProxyHandler() {

                @Override
                protected <T> T handle(Class<T> proxyClass) {
                    try {
                        ServiceMetadata serviceMetadata = new ServiceMetadata(serverKey);
                        ConnectWatch connectWatch = nettyConsumer.watchConnect(serviceMetadata);
                        // 连接等待
                        boolean isConnected = connectWatch.waitForAvailable(waitMills);
                        if (!isConnected) {
                            return null;
                        }
                        return ProxyObjectFactory
                                .factory()
                                .isAsync(isAsync)
                                .consumer(nettyConsumer)
                                .metadata(serviceMetadata)
                                .connectWatch(connectWatch)
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

    private void configCenter() throws RemoteTimeoutException {
        try {
            Requires.isNull(configKey, "config.key");
            ServiceMetadata configCenter = new ServiceMetadata(configKey);
            ConnectWatch connectWatch = nettyConsumer.watchConnect(configCenter);
            boolean available = connectWatch.waitForAvailable(30000);
            if (!available) {
                throw new RemoteTimeoutException("unable to connect config center, because: timeout");
            }

            // 初始化查询配置文件
            String config = (String) ProxyObjectFactory
                    .factory()
                    .isAsync(false)
                    .consumer(nettyConsumer)
                    .metadata(configCenter)
                    .connectWatch(connectWatch)
                    .remoteInvoke("RemoteConfigService", "queryConfigByServerCodeToJson", new String[]{consumerKey});
            if (StringUtils.isNotEmpty(config)) {
                JSONObject configObj = JSONObject.parseObject(config);
                // 解析配置数据
                String serverCode = configObj.getString("serverCode");
                if (serverCode.equals(consumerKey)) {
                    // 1.数据源
                    boolean isDataSource = configObj.containsKey("datasourceModel");
                    if (isDataSource) {
                        JSONObject datasourceModel = configObj.getJSONObject("datasourceModel");
                    }
                    // 2.配置
                    configChanged(configObj);
                    // 同步缓存中的数据
                    Property.synchronous(applicationContext.getEnvironment());
                    // 通知刷新配置数据
                    applicationContext.publishEvent(new PropertiesRefreshEvent(applicationContext));
                }
            }

            RepeatableFuture<String> addFuture = ConfigListeners.onAdd(configKey);
            addFuture.addListener(new FutureListener<String>() {
                @Override
                public void completed(String result, Throwable throwable) throws Exception {
                    if (StringUtils.isNotEmpty(result)) {
                        JSONObject configObj = JSONObject.parseObject(result);
                        String serverCode = configObj.getString("serverCode");
                        if (!serverCode.equals(consumerKey)) {
                            return;
                        }
                        configChanged(configObj);
                        // 同步缓存中的数据
                        Property.synchronous(applicationContext.getEnvironment());
                        // 通知刷新配置数据
                        applicationContext.publishEvent(new PropertiesRefreshEvent(applicationContext));
                    }
                }
            });

            RepeatableFuture<String> deleteFuture = ConfigListeners.onDelete(configKey);
            deleteFuture.addListener(new FutureListener<String>() {
                @Override
                public void completed(String result, Throwable throwable) throws Exception {
                    if (StringUtils.isNotEmpty(result)) {
                        JSONObject configObj = JSONObject.parseObject(result);
                        String serverCode = configObj.getString("serverCode");
                        if (!serverCode.equals(consumerKey)) {
                            return;
                        }
                        if (!configObj.containsKey("configModel")) {
                            return;
                        }
                        JSONArray configModels = configObj.getJSONArray("configModel");
                        for (int i = 0; i < configModels.size(); i++) {
                            JSONObject configModel = configModels.getJSONObject(i);
                            String configName = configModel.getString("configName");
                            Environment environment = applicationContext.getEnvironment();
                            if (environment instanceof ConfigurableEnvironment) {
                                ((ConfigurableEnvironment) environment).getPropertySources().remove(configName);
                            }
                        }
                        // 同步缓存中的数据
                        Property.synchronous(applicationContext.getEnvironment());
                        // 通知刷新配置数据
                        applicationContext.publishEvent(new PropertiesRefreshEvent(applicationContext));
                    }
                }
            });

            RepeatableFuture<String> updateFuture = ConfigListeners.onUpdate(configKey);
            updateFuture.addListener(new FutureListener<String>() {
                @Override
                public void completed(String result, Throwable throwable) throws Exception {
                    if (StringUtils.isNotEmpty(result)) {
                        JSONObject configObj = JSONObject.parseObject(result);
                        String serverCode = configObj.getString("serverCode");
                        if (!serverCode.equals(consumerKey)) {
                            return;
                        }
                        if (!configObj.containsKey("configModel")) {
                            return;
                        }
                        JSONArray configModels = configObj.getJSONArray("configModel");
                        for (int i = 0; i < configModels.size(); i++) {
                            JSONObject configModel = configModels.getJSONObject(i);
                            String configName = configModel.getString("configName");
                            Environment environment = applicationContext.getEnvironment();
                            if (environment instanceof ConfigurableEnvironment) {
                                ((ConfigurableEnvironment) environment).getPropertySources().remove(configName);
                            }
                            String configJson = configModel.getString("configJson");
                            Properties properties = ConfigParser.getProperties(configJson);
                            PropertiesPropertySource propertySource = new PropertiesPropertySource(configName, properties);
                            addProperties(propertySource, applicationContext.getEnvironment());
                        }
                        // 同步缓存中的数据
                        Property.synchronous(applicationContext.getEnvironment());
                        // 通知刷新配置数据
                        applicationContext.publishEvent(new PropertiesRefreshEvent(applicationContext));
                    }
                }
            });

        } catch (IllegalArgumentException | InterruptedException | ConnectionException e) {
            logger.info("unable to connect config center, because: {}", e.getMessage());
        }
    }

    private void configChanged(JSONObject configObj) {
        if (!configObj.containsKey("configModel")) {
            return;
        }
        JSONArray configModels = configObj.getJSONArray("configModel");
        for (int i = 0; i < configModels.size(); i++) {
            JSONObject configModel = configModels.getJSONObject(i);
            String configName = configModel.getString("configName");
            String configJson = configModel.getString("configJson");
            Properties properties = ConfigParser.getProperties(configJson);
            PropertiesPropertySource propertySource = new PropertiesPropertySource(configName, properties);
            addProperties(propertySource, applicationContext.getEnvironment());
        }
    }

    /**
     * 向spring上下文添加配置
     * @param propertySource
     * @param environment
     */
    private void addProperties(PropertiesPropertySource propertySource, Environment environment) {
        String name = propertySource.getName();
        MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
        if (propertySources.contains(name)) {
            PropertySource<?> existing = propertySources.get(name);
            PropertySource<?> newSource = (propertySource instanceof ResourcePropertySource ?
                    ((ResourcePropertySource) propertySource).withResourceName() : propertySource);
            if (existing instanceof CompositePropertySource) {
                ((CompositePropertySource) existing).addFirstPropertySource(newSource);
            }
            else {
                if (existing instanceof ResourcePropertySource) {
                    existing = ((ResourcePropertySource) existing).withResourceName();
                }
                CompositePropertySource composite = new CompositePropertySource(name);
                composite.addPropertySource(newSource);
                composite.addPropertySource(existing);
                propertySources.replace(name, composite);
            }
        } else {
            if (name == null) {
                propertySources.addFirst(propertySource);
            } else {
                propertySources.addLast(propertySource);
            }
        }
    }



    @Override
    public void destroy() throws Exception {
        nettyConsumer.stop();
    }

    public NettyConsumer getConsumer() {
        return nettyConsumer;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Environment environment = applicationContext.getEnvironment();
        if (environment instanceof ConfigurableEnvironment) {
            PropertySource<?> propertySource = ((ConfigurableEnvironment) environment).getPropertySources().get("consumer");
            if (propertySource != null) {
                goon = true;
                Property.addResource(new TypePropertySource<>(propertySource));
                Property.refreshProperties(this);
            }
        }
        this.applicationContext = applicationContext;
    }

    public ConsumerWrapper getConsumerWrapper() {
        return consumerWrapper;
    }

    public Map<Class<?>, ProxyHandler> getProxies() {
        return proxies;
    }

    public void setConsumerWrapper(ConsumerWrapper consumerWrapper) {
        this.consumerWrapper = consumerWrapper;
    }

    static class ConsumerBean<T> implements FactoryBean<T> {

        private final Class<T> clazz;

        private final ProxyHandler handler;

        public ConsumerBean(Class<T> clazz, ProxyHandler handler) {
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
