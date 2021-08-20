package com.jw.screw.common.proxy;

import net.sf.cglib.proxy.MethodProxy;
import org.junit.Test;

import java.lang.reflect.Method;

public class CglibProxyTest {

    @Test
    public void interfaceInvocation() {
        DemoServiceImpl demoService = ProxyFactory.proxy().newProxyInstance(DemoServiceImpl.class, new InvocationInterceptor() {
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

        demoService.noResult("2121", 2121);
    }

    @Test
    public void subClass() {
        ProxyService proxyService = ProxyFactory.proxy().newProxyInstance(ProxyService.class, new CglibInvocationInterceptor() {
            @Override
            public Object invoke(Object object, Method method, Object[] args, MethodProxy proxy) {
                Object o = null;
                try {
                    o = proxy.invokeSuper(object, args);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return o;
            }
        });
        String test = proxyService.test();
        System.out.println(test);
    }
}
