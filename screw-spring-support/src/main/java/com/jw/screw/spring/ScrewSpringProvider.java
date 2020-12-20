package com.jw.screw.spring;

import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.exception.RemoteException;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.util.Requires;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.provider.NettyProvider;
import com.jw.screw.provider.NettyProviderConfig;
import com.jw.screw.provider.annotations.ProviderService;
import com.jw.screw.remote.Remotes;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import com.jw.screw.spring.anntation.ScrewValue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.*;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author jiangw
 */
public class ScrewSpringProvider implements InitializingBean, ApplicationContextAware, DisposableBean {

    @ScrewValue("provider-provider.packageScan")
    private String packageScan;

    @ScrewValue("provider-registry.address")
    private String registryAddress;

    @ScrewValue("provider-registry.port")
    private Integer registryPort;

    @ScrewValue("provider-provider.providerKey")
    private String providerKey;

    @ScrewValue("provider-provider.port")
    private Integer port;

    @ScrewValue("provider-provider.weight")
    private Integer weight;

    @ScrewValue("provider-provider.connCount")
    private Integer connCount;

    private NettyProvider nettyProvider;

    private final Set<Object> publishServices = new CopyOnWriteArraySet<>();

    /**
     * 是否开启provider
     */
    private boolean goon = false;

    private ApplicationContext applicationContext;

    private final ExecutorService nettyServer = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            new NamedThreadFactory("screw server"));

    public ScrewSpringProvider() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 不开启provider
        if (!goon) {
            return;
        }
        // 验证参数
        Requires.isNull(providerKey, "providerKey");
        Requires.isNull(registryAddress, "registryAddress");
        Requires.isNull(registryPort, "registryPort");
        Requires.isNull(port, "port");

        NettyProviderConfig providerConfig = new NettyProviderConfig();
        providerConfig.setProviderKey(providerKey);
        providerConfig.setWeight(weight);
        providerConfig.setConnCount(connCount);
        providerConfig.setPort(port);
        NettyClientConfig registryClientConfig = new NettyClientConfig();
        // 判断注册中心是否可连接
        boolean connectable = Remotes.connectable(registryAddress, registryPort, 300000);
        if (!connectable) {
            // 阻塞一段时间，再次尝试
            synchronized (this) {
                this.wait(3000);
            }
            connectable = Remotes.connectable(registryAddress, registryPort, 300000);
            if (!connectable) {
                throw new RemoteException("registry can't connect");
            }
        }
        registryClientConfig.setDefaultAddress(new RemoteAddress(registryAddress, registryPort));
        providerConfig.setRegisterConfig(registryClientConfig);
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
        nettyServer.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    nettyProvider.start();
                } catch (InterruptedException e) {
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
                goon = true;
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
        nettyProvider.shutdown();

        nettyServer.shutdown();
    }

    /**
     * 发布服务，如果在server启动过程中已经发布的服务不会在重新发布
     * @param publishService
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
        try {
            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            String[] packages = packageScan.split(StringPool.COMMA);
            for (String aPackage : packages) {
                Resource[] resources = resourcePatternResolver.getResources(aPackage);
                for (Resource resource : resources) {
                    MetadataReaderFactory metadata = new SimpleMetadataReaderFactory();
                    MetadataReader metadataReader = metadata.getMetadataReader(resource);
                    AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
                    Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(ProviderService.class.getName());
                    if (!CollectionUtils.isEmpty(annotationAttributes)) {
                        String className = annotationMetadata.getClassName();
                        publishServices.add(Class.forName(className).newInstance());
                    }
                }
            }
        } catch (FileNotFoundException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    private final class ProviderServiceListener implements ApplicationListener<ApplicationEvent> {

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ContextRefreshedEvent) {
                nettyProvider.publishServices(publishServices.toArray(new Object[0]));
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
