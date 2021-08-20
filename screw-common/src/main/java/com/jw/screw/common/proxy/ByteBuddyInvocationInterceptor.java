package com.jw.screw.common.proxy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 针对子类继承的拦截器
 * @author jiangw
 * @date 2021/8/13 10:56
 * @since 1.0
 */
public interface ByteBuddyInvocationInterceptor extends InvocationInterceptor {

    /**
     * @param callable 可调用父类方法
     * @see #invoke(Object, Method, Object[])
     */
    Object invoke(Object proxy, Method method, Object[] args, Callable<?> callable);

    @Override
    default Object invoke(Object proxy, Method method, Object[] args) {
        return null;
    }
}
