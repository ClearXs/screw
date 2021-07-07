package com.jw.screw.spring.boot;

import com.jw.screw.spring.ConsumerWrapper;
import com.jw.screw.spring.FactoryBeanRegisterProcessor;
import com.jw.screw.spring.ScrewSpringConsumer;
import com.jw.screw.spring.event.ConnectionRejectListener;
import com.jw.screw.spring.event.RefreshListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.List;

/**
 * consumer 配置类
 * @author jiangw
 * @date 2021/1/9 10:46
 * @since 1.0
 */
@Configuration
public class ScrewConsumerConfiguration {

    @Bean
    public List<ConsumerWrapper.ServiceWrapper> serviceWrapper() {
        return null;
    }

    @Bean
    public ConsumerWrapper consumerWrapper(@Qualifier("serviceWrapper") List<ConsumerWrapper.ServiceWrapper> serviceWrappers) {
        ConsumerWrapper consumerWrapper = new ConsumerWrapper();
        consumerWrapper.setServiceWrappers(serviceWrappers);
        return consumerWrapper;
    }

    @Bean
    public RefreshListener refreshListener() {
        return new RefreshListener();
    }

    @Bean
    public ConnectionRejectListener rejectListener() {
        return new ConnectionRejectListener();
    }

    @Bean
    @DependsOn({"bootRefreshContext", "refreshListener", "rejectListener"})
    public ScrewSpringConsumer screwConsumer(@Qualifier("consumerWrapper") ConsumerWrapper consumerWrapper) {
        ScrewSpringConsumer screwConsumer = new ScrewSpringConsumer();
        screwConsumer.setConsumerWrapper(consumerWrapper);
        return screwConsumer;
    }

    @Bean
    public FactoryBeanRegisterProcessor factory(@Qualifier("screwConsumer") ScrewSpringConsumer screwConsumer) {
        FactoryBeanRegisterProcessor processor = new FactoryBeanRegisterProcessor();
        processor.setProxies(screwConsumer.getProxies());
        return processor;
    }
}
