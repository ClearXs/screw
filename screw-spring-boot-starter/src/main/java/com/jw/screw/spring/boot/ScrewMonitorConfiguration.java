package com.jw.screw.spring.boot;

import com.jw.screw.spring.ScrewSpringMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * monitor 配置类
 * @author jiangw
 * @date 2021/1/9 11:41
 * @since 1.0
 */
@Configuration
public class ScrewMonitorConfiguration {

    @Bean
    @DependsOn("screwRegistry")
    public ScrewSpringMonitor screwMonitor() {
        return new ScrewSpringMonitor();
    }
}
