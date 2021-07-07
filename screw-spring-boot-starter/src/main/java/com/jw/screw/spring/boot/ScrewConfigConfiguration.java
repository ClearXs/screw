package com.jw.screw.spring.boot;

import com.jw.screw.spring.ScrewSpringProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置中心配置类
 * @author jiangw
 * @date 2021/1/13 16:32
 * @since 1.0
 */
@Configuration
public class ScrewConfigConfiguration {

    @Bean
    public ScrewSpringProvider screwConfig() {
        return new ScrewSpringProvider();
    }
}
