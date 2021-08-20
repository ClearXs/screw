package com.jw.screw.common.proxy;

import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * cglib的代理实现
 * @author jiangw
 * @date 2021/8/12 16:53
 * @since 1.1
 */
public interface CglibInvocationInterceptor extends InvocationInterceptor {

    /**
     * @see InvocationInterceptor#invoke(Object, Method, Object[])
     * @param proxy 原对象的方法
     * @return 调用接口
     */
    Object invoke(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable;

    @Override
    default Object invoke(Object proxy, Method method, Object[] args) {
        return null;
    }
}
