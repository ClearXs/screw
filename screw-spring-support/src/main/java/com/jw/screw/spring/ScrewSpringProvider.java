package com.jw.screw.spring;

import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.SystemConfig;
import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.exception.RemoteException;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.util.Requires;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.provider.NettyProvider;
import com.jw.screw.provider.NettyProviderConfig;
import com.jw.screw.provider.annotations.ProviderService;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import com.jw.screw.spring.anntation.ScrewValue;
import com.jw.screw.spring.config.Property;
import com.jw.screw.spring.config.TypePropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author jiangw
 */
public class ScrewSpringProvider implements com.jw.screw.spring.ScrewSpring {

    @ScrewValue("provider-enable")
    private Boolean enable;

    /**
     * 本身服务的key
     */
    @ScrewValue("provider-server.key")
    private String serverKey;

    /**
     * 本身服务的port
     */
    @ScrewValue("provider-provider.server.port")
    private Integer serverPort;

    /**
     * 服务地址
     */
    @ScrewValue("provider-provider.address")
    private String providerAddress;

    /**
     * 注册中心host
     */
    @ScrewValue("provider-registry.address")
    private String registryAddress;

    /**
     * 注册中心端口
     */
    @ScrewValue("provider-registry.port")
    private Integer registryPort;

    /**
     * 服务权重
     */
    @ScrewValue("provider-weight")
    private Integer weight;

    /**
     * 最多支持连接数
     */
    @ScrewValue("provider-connCount")
    private Integer connCount;

    /**
     * 配置中心
     */
    @ScrewValue("provider-config.key")
    private String configServerKey;

    /**
     * 监控中心
     */
    @ScrewValue("provider-monitor.key")
    private String monitorServerKey;

    /**
     * 监控指标收集周期
     */
    @ScrewValue("provider-monitor.collect.period")
    private Integer monitorCollectPeriod;

    /**
     * provider service packages
     */
    @ScrewValue("provider-provider.packageScan")
    private String packageScan;

    /**
     * @see NettyProvider
     */
    private NettyProvider nettyProvider;

    /**
     * @see ApplicationContext
     */
    private ApplicationContext applicationContext;

    private final Set<Object> publishServices = new CopyOnWriteArraySet<>();

    /**
     * @see NettyProviderConfig
     */
    private NettyProviderConfig providerConfig;

    private static Logger logger = LoggerFactory.getLogger(ScrewSpringProvider.class);

    private final ExecutorService provider = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            new NamedThreadFactory("screw provider"));

    @Override
    public void validateParams() {
        // 验证参数
        Requires.isNull(serverKey, "providerKey");
        Requires.isNull(registryAddress, "registryAddress");
        Requires.isNull(registryPort, "registryPort");
        Requires.isNull(serverPort, "port");
    }

    @Override
    public void initConfig() throws IOException, InterruptedException, RemoteException {
        // 基本配置
        providerConfig.setServerKey(serverKey);
        providerConfig.setPort(serverPort);
        providerConfig.setWeight(weight);
        providerConfig.setConnCount(connCount);
        providerConfig.setRole(SystemConfig.PROVIDER.getRole());
        NettyClientConfig registryClientConfig = new NettyClientConfig();
        registryClientConfig.setDefaultAddress(new RemoteAddress(registryAddress, registryPort));
        providerConfig.setServerHost(providerAddress);
        providerConfig.setRegisterConfig(registryClientConfig);
        // 配置中心地址
        providerConfig.setConfigServerKey(configServerKey);
        // 监控中心地址
        providerConfig.setMonitorServerKey(monitorServerKey);
        providerConfig.setMonitorCollectPeriod(monitorCollectPeriod);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!enable) {
            return;
        }
        validateParams();
        providerConfig = new NettyProviderConfig();
        initConfig();
        nettyProvider = new NettyProvider(providerConfig);
        // 判断交给spring容器中的Bean是否有ProviderService注解。如果有则添加到待发布的Service中
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);
            boolean isProviderService = bean.getClass().isAnnotationPresent(ProviderService.class);
            if (isProviderService) {
                publishServices.add(bean);
            }
        }
        // 如果发布的服务不在spring容器下，此时需要自动扫描实现。提供的服务必须有无参构造器
        packageScan();
        provider.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    nettyProvider.start();
                } catch (InterruptedException | ConnectionException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Environment environment = applicationContext.getEnvironment();
        if (environment instanceof ConfigurableEnvironment) {
            PropertySource<?> propertySource = ((ConfigurableEnvironment) environment).getPropertySources().get("provider");
            if (propertySource != null) {
                Property.addResource(new TypePropertySource<>(propertySource));
                Property.refreshProperties(this);
            }
        }
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) applicationContext).addApplicationListener(new ProviderServiceListener());
        }
        this.applicationContext = applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        if (nettyProvider != null) {
            nettyProvider.shutdown();
        }
        provider.shutdown();
    }

    /**
     * 发布服务，如果在server启动过程中已经发布的服务不会在重新发布
     * @param publishService service
     */
    public void publish(Object... publishService) {
        for (Object o : publishService) {
            applicationContext.publishEvent(new ProviderServiceEvent(o));
        }
    }

    public NettyProvider getProvider() {
        return nettyProvider;
    }

    private void packageScan() throws IOException {
        if (StringUtils.isEmpty(packageScan)) {
            return;
        }
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        String[] packages = packageScan.split(StringPool.COMMA);
        for (String aPackage : packages) {
            Resource[] resources = resourcePatternResolver.getResources(aPackage);
            for (Resource resource : resources) {
                MetadataReaderFactory metadata = new SimpleMetadataReaderFactory();
                MetadataReader metadataReader = metadata.getMetadataReader(resource);
                AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
                Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(ProviderService.class.getName());
                if (CollectionUtils.isEmpty(annotationAttributes)) {
                    continue;
                }
                String className = annotationMetadata.getClassName();
                try {
                    publishServices.add(Class.forName(className).newInstance());
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final class ProviderServiceListener implements ApplicationListener<ApplicationEvent> {

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ContextRefreshedEvent) {
                if (nettyProvider != null) {
                    nettyProvider.publishServices(publishServices.toArray(new Object[0]));
                }
            }
            if (event instanceof ProviderServiceEvent) {
                Object service = event.getSource();
                boolean add = publishServices.add(service);
                if (add) {
                    nettyProvider.publishServices(service);
                }
            }
        }
    }

    private static final class ProviderServiceEvent extends ApplicationEvent {

        /**
         * Create a new ApplicationEvent.
         *
         * @param source the component that published the event (never {@code null})
         */
        public ProviderServiceEvent(Object source) {
            super(source);
        }
    }
}
