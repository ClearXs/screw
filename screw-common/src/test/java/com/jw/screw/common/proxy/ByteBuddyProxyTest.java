package com.jw.screw.common.proxy;

import org.junit.Test;

public class ByteBuddyProxyTest {

    @Test
    public void testInstance() {
        DemoService demoService = ProxyFactory.proxy().newProxyInstance(DemoService.class, (proxy, method, args) -> {
            System.out.println(proxy);
            return null;
        }, true);

        demoService.hello();
    }

    /**
     * byte buddy代理实现接口
     */
    @Test
    public void interfaceInvocation() {
        DemoService demoService = ProxyFactory.proxy().newProxyInstance(DemoService.class, (proxy, method, args) -> {
            if (method.getName().equals("hello")) {
                return "hello";
            } else {
                for (Object arg : args) {
                    System.out.println(arg);
                }
            }
            return null;
        }, true);

        String hello = demoService.hello();
        System.out.println(hello);

        demoService.noResult(2112, "2121", 1f);
    }

    /**
     * byte buddy继承子类
     */
    @Test
    public void subClassInvocation() {
        DemoServiceImpl demoService = ProxyFactory.proxy().newProxyInstance(DemoServiceImpl.class, (ByteBuddyInvocationInterceptor) (proxy, method, args, callable) -> {
            try {
                return callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, true);
        String hello = demoService.hello();
        System.out.println(hello);
        demoService.noResult("2121");
    }
}
