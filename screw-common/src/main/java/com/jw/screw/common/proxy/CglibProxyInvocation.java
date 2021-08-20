package com.jw.screw.common.proxy;

import com.jw.screw.common.util.ClassUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * cglib代理实现
 * @author jiangw
 * @date 2021/8/12 16:53
 * @since 1.1
 */
public class CglibProxyInvocation implements ProxyInvocation {

    @Override
    public <T> T proxyInstance(ClassLoader classLoader, Class<T> target, InvocationInterceptor interceptor, Object[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                if (interceptor instanceof CglibInvocationInterceptor) {
                    return ((CglibInvocationInterceptor) interceptor).invoke(obj, method, args, proxy);
                }
                return interceptor.invoke(obj, method, args);
            }
        });
        Object proxy;
        if (args != null && args.length != 0) {
            proxy = enhancer.create(ClassUtils.objectToClass(args), args);
        } else {
            proxy = enhancer.create();
        }
        return target.cast(proxy);
    }
}
