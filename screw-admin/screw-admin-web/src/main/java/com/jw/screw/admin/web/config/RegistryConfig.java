package com.jw.screw.admin.web.config;

import com.jw.screw.spring.NullablePropertySourceFactory;
import com.jw.screw.spring.ScrewSpringRegistry;
import com.jw.screw.spring.event.RefreshListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 注册中心配置类
 * @author jiangw
 * @date 2020/12/10 17:37
 * @since 1.0
 */
@Configuration
@PropertySource(name = "registry", value = {
        "classpath:prop/screw-registry.properties"
}, factory = NullablePropertySourceFactory.class)
public class RegistryConfig {

    @Bean
    public ScrewSpringRegistry screwRegistry() {
        return new ScrewSpringRegistry();
    }

    @Bean
    public RefreshListener refreshListener() {
        return new RefreshListener();
    }
}
