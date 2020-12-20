package com.jw.screw.test.spring;

import com.jw.screw.spring.ScrewSpringProvider;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.TimeUnit;

public class SpringProviderTest {

    @Test
    public void provider() throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ProviderConfigTest.class);
        ScrewSpringProvider provider = applicationContext.getBean(ScrewSpringProvider.class);

        TimeUnit.SECONDS.sleep(100);
    }
}
