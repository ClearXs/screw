package com.jw.screw.spring.boot;

import com.jw.screw.spring.ScrewSpringRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * registry 配置类
 * @author jiangw
 * @date 2021/1/9 11:41
 * @since 1.0
 */
@Configuration
public class ScrewRegistryConfiguration {

    @Bean
    public ScrewSpringRegistry screwRegistry() {
        return new ScrewSpringRegistry();
    }
}
