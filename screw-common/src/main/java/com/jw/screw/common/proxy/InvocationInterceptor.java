package com.jw.screw.common.proxy;

import java.lang.reflect.Method;

/**
 * <b>InvocationInterceptor是统一代理的具体实现</b>
 * <p>统一了jdk、cglib、byte-buddy的代理实现</p>
 * @author jiangw
 * @date 2021/8/12 16:06
 * @since 1.1
 */
@FunctionalInterface
public interface InvocationInterceptor {

    /**
     * 代理对象调用方法时进行的回调
     * @param proxy 代理对象 or 增强的对象
     * @param method 调用的方法
     * @param args 方法参数
     * @return 调用结果
     */
    Object invoke(Object proxy, Method method, Object[] args) throws InterruptedException;
}
