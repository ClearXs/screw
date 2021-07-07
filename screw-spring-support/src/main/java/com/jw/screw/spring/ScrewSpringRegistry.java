package com.jw.screw.spring;

import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.SystemConfig;
import com.jw.screw.registry.DefaultRegistry;
import com.jw.screw.registry.Registry;
import com.jw.screw.spring.anntation.ScrewValue;
import com.jw.screw.spring.config.Property;
import com.jw.screw.spring.config.TypePropertySource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * screw
 * @author jiangw
 * @date 2020/12/9 14:09
 * @since 1.0
 */
public class ScrewSpringRegistry implements ScrewSpring {

    @ScrewValue("registry-registry.port")
    private Integer registryPort;

    private Registry registry;

    private final ExecutorService registryExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            new NamedThreadFactory("screw registry"));

    @Override
    public void afterPropertiesSet() throws Exception {
        if (registryPort == null) {
            registryPort = SystemConfig.REGISTRY_CENTER.getDefaultPort();
        }
        if (registryPort == null) {
            throw new NullPointerException("registry port is null");
        }
        registry = new DefaultRegistry(registryPort);
        registryExecutor.submit(new Runnable() {
            @Override
            public void run() {
                registry.start();
            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Environment environment = applicationContext.getEnvironment();
        if (environment instanceof ConfigurableEnvironment) {
            PropertySource<?> propertySource = ((ConfigurableEnvironment) environment).getPropertySources().get("registry");
            if (propertySource != null) {
                Property.addResource(new TypePropertySource<>(propertySource));
                Property.refreshProperties(this);
            }
        }
    }

    public int getRegistryPort() {
        return registryPort;
    }

    @Override
    public void destroy() throws Exception {
        if (registry != null) {
            registry.shutdown();
        }
        registryExecutor.shutdownNow();
    }

    @Override
    public void initConfig() {

    }

    @Override
    public void validateParams() {

    }
}
