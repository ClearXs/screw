package com.jw.screw.test.spring;

import com.jw.screw.spring.NullablePropertySourceFactory;
import com.jw.screw.spring.ScrewSpringMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(name = "monitor", value = {
        "classpath:prop/screw-monitor.properties"
}, factory = NullablePropertySourceFactory.class)
public class MonitorConfigTest {

    @Bean
    @DependsOn(value = {"screwRegistry"})
    public ScrewSpringMonitor screwSpringProvider() {
        return new ScrewSpringMonitor();
    }
}
