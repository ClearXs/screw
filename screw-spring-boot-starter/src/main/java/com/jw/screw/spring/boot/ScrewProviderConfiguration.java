package com.jw.screw.spring.boot;

import com.jw.screw.spring.ScrewSpringProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * provider 配置类
 * @author jiangw
 * @date 2021/1/9 11:38
 * @since 1.0
 */
@Configuration
public class ScrewProviderConfiguration {

    @Bean
    public ScrewSpringProvider screwProvider() {
        return new ScrewSpringProvider();
    }
}
