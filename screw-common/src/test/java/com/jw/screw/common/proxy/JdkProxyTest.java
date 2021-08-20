package com.jw.screw.common.proxy;

import org.junit.Test;

import java.lang.reflect.Method;

public class JdkProxyTest {

    @Test
    public void interfaceInvocation() {
        DemoService demoService = ProxyFactory.proxy().newProxyInstance(DemoService.class, new InvocationInterceptor() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                if (args != null) {
                    for (Object arg : args) {
                        System.out.println(arg);
                    }
                }
                if (method.getName().equals("hello")) {
                    return "hello";
                }

                return null;
            }
        });

        String hello = demoService.hello();
        System.out.println(hello);

        demoService.noResult(2121, "2121");
    }
}
