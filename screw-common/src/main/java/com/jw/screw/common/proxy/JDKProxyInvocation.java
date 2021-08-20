package com.jw.screw.common.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * jdk代理对象实现
 * @author jiangw
 * @date 2021/8/12 16:37
 * @since 1.1
 */
class JDKProxyInvocation implements ProxyInvocation {

    @Override
    public <T> T proxyInstance(ClassLoader classLoader, Class<T> target, InvocationInterceptor interceptor, Object[] args) throws InstantiationException, IllegalAccessException {
        Class<?>[] interfaces = target.isInterface() ? new Class[] { target } : target.getInterfaces();
        Object proxy = Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return interceptor.invoke(proxy, method, args);
            }
        });
        return target.cast(proxy);
    }
}
