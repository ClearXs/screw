package com.jw.screw.test.spring;

import com.jw.screw.spring.NullablePropertySourceFactory;
import com.jw.screw.spring.ScrewSpringRegistry;
import com.jw.screw.spring.event.RefreshListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(name = "registry", value = {
        "classpath:prop/screw-registry.properties"
}, factory = NullablePropertySourceFactory.class)
public class RegistryConfigTest {

    @Bean
    public ScrewSpringRegistry screwRegistry() {
        return new ScrewSpringRegistry();
    }

    @Bean
    public RefreshListener refreshListener() {
        return new RefreshListener();
    }
}
