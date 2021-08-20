package com.jw.screw.common.proxy;

import org.junit.Test;

import java.lang.reflect.Method;

public class PerformanceTest {

    private void performance(int cycles) {

        // jdk
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < cycles; i++) {
            DemoService jdkDemoService = ProxyFactory.proxy().newProxyInstance(DemoService.class, new InvocationInterceptor() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if (method.getName().equals("hello")) {
                        return "hello";
                    }
                    return null;
                }
            });
            jdkDemoService.hello();
            jdkDemoService.noResult(2121, "2121");
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("%s jdk consume time: %s%n", cycles, endTime - startTime);

        // cglib
        startTime = System.currentTimeMillis();
        for (int i = 0; i < cycles; i++) {
            DemoServiceImpl cglibDemoService = ProxyFactory.proxy().newProxyInstance(DemoServiceImpl.class, new InvocationInterceptor() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if (method.getName().equals("hello")) {
                        return "hello";
                    }
                    return null;
                }
            });
            cglibDemoService.hello();
            cglibDemoService.noResult("2121", 2121);
        }
        endTime = System.currentTimeMillis();
        System.out.printf("%s cglib consume time: %s%n", cycles, endTime - startTime);

        // byte buddy
        startTime = System.currentTimeMillis();
        for (int i = 0; i < cycles; i++) {
            DemoService byteBuddyDemoService = ProxyFactory.proxy().newProxyInstance(DemoService.class, (proxy, method, args) -> {
                if (method.getName().equals("hello")) {
                    return "hello";
                }
                return null;
            }, true);

            byteBuddyDemoService.hello();

            byteBuddyDemoService.noResult(2121, "2121");
        }
        endTime = System.currentTimeMillis();
        System.out.printf("%s byte buddy consume time: %s%n", cycles, endTime - startTime);
    }

    @Test
    public void testSimple() {
        performance(100);

        performance(1000);

        performance(10000);

        // 测试结果发现 jdk是最快的，cglib慢jdk10倍，byte buddy慢cglib10倍
        // 但byte buddy提供了很多便捷的api对字节码进行操作
    }
}
