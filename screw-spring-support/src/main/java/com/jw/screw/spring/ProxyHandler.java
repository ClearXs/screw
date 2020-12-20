package com.jw.screw.spring;

/**
 * @author jiangw
 */
public abstract class ProxyHandler {

    /**
     * 根据不同的代理实现
     * @param proxyClass
     * @param <T>
     * @return
     */
    protected abstract <T> T handle(Class<T> proxyClass);
}
