package com.jw.screw.spring.boot;

import com.jw.screw.spring.ScrewSpringProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 配置中心配置类
 * @author jiangw
 * @date 2021/1/13 16:32
 * @since 1.0
 */
@Configuration
public class ScrewConfigConfiguration {

    @Bean
    @DependsOn("screwRegistry")
    public ScrewSpringProvider screwConfig() {
        return new ScrewSpringProvider();
    }
}
