package com.jw.screw.test.spring;

import com.jw.screw.spring.ScrewSpringRegistry;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringRegistryTest {

    @Test
    public void registry() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(RegistryConfigTest.class);

        ScrewSpringRegistry bean = applicationContext.getBean(ScrewSpringRegistry.class);
        int registryPort = bean.getRegistryPort();
        System.out.println(registryPort);
    }
}
