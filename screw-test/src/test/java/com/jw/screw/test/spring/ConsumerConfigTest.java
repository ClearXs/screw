package com.jw.screw.test.spring;

import com.jw.screw.spring.FactoryBeanRegisterProcessor;
import com.jw.screw.spring.NullablePropertySourceFactory;
import com.jw.screw.spring.ScrewSpringConsumer;
import com.jw.screw.spring.config.ValueRefreshContext;
import com.jw.screw.spring.event.RefreshListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(name = "consumer", value = {
        "classpath:prop/screw-consumer.properties"
}, factory = NullablePropertySourceFactory.class)
public class ConsumerConfigTest {

    @Bean
    public ScrewSpringConsumer screwConsumer() {
        return new ScrewSpringConsumer();
    }

    @Bean
    public FactoryBeanRegisterProcessor factory(@Qualifier("screwConsumer") ScrewSpringConsumer screwConsumer) {
        FactoryBeanRegisterProcessor processor = new FactoryBeanRegisterProcessor();
        processor.setProxies(screwConsumer.getProxies());
        return processor;
    }

    @Bean
    public RefreshListener refreshListener() {
        return new RefreshListener();
    }

    @Bean
    public ValueRefreshContext valueRefreshContext() {
        return new ValueRefreshContext();
    }
}
