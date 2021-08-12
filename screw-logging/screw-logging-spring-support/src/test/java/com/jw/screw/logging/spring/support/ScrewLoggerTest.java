package com.jw.screw.logging.spring.support;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ScrewLoggerTest {

    @Test
    public void test() throws InterruptedException {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:application.xml");
        System.out.println(applicationContext);
        DemoService bean = applicationContext.getBean(DemoService.class);
        bean.hello();

        bean.message();

        bean.exception();
        Thread.currentThread().join();
    }
}
