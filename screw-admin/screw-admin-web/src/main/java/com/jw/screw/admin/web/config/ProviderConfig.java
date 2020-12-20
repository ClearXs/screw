package com.jw.screw.admin.web.config;

import com.jw.screw.spring.NullablePropertySourceFactory;
import com.jw.screw.spring.ScrewSpringProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

/**
 * 服务提供的配置类
 * @author jiangw
 * @date 2020/12/10 17:36
 * @since 1.0
 */
@Configuration
@PropertySource(name = "provider", value = {
        "classpath:prop/screw-provider.properties"
}, factory = NullablePropertySourceFactory.class)
public class ProviderConfig {

    @Bean
    @DependsOn(value = {"screwRegistry"})
    public ScrewSpringProvider screwProvider() {
        return new ScrewSpringProvider();
    }
}
