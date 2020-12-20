package com.jw.screw.test.spring;

import com.jw.screw.spring.ConsumerWrapper;
import com.jw.screw.spring.NullablePropertySourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@PropertySource(name = "consumer", value = {
        "classpath:prop/screw-consumer.properties"
}, factory = NullablePropertySourceFactory.class)
public class ConsumerConfigTest {

    @Bean
    public List<ConsumerWrapper.ServiceWrapper> serviceWrapper() {
        List<ConsumerWrapper.ServiceWrapper> wrappers = new ArrayList<>();
        ConsumerWrapper.ServiceWrapper serviceWrapper = new ConsumerWrapper.ServiceWrapper();
        serviceWrapper.setServices(Collections.singletonList(DemoService.class));
        serviceWrapper.setServerKey("provider1");
        wrappers.add(serviceWrapper);
        return wrappers;
    }
}
