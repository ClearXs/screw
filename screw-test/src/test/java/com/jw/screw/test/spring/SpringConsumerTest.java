package com.jw.screw.test.spring;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringConsumerTest {

    @Test
    public void consumer() throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ConsumerConfigTest.class);
        DemoService bean = applicationContext.getBean(DemoService.class);
        String hello = bean.hello("2121");

        System.out.println(hello);

        hello = bean.hello("323232");
        System.out.println(hello);

        applicationContext.destroy();
    }
}
