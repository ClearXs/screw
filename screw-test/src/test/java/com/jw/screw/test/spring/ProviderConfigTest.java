package com.jw.screw.test.spring;

import com.jw.screw.spring.NullablePropertySourceFactory;
import com.jw.screw.spring.ScrewSpringProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(name = "provider", value = {
        "classpath:prop/screw-provider.properties"
}, factory = NullablePropertySourceFactory.class)
public class ProviderConfigTest {

    @Bean
    @DependsOn(value = {"screwRegistry"})
    public ScrewSpringProvider screwSpringProvider() {
        return new ScrewSpringProvider();
    }
}
