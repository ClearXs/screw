package com.jw.screw.common.proxy;

/**
 * 代理实例的创建工厂
 * @author jiangw
 * @date 2021/8/13 11:11
 * @since 1.0
 */
public class ProxyFactory {

    private final ProxyContext proxyContext = new ProxyContext();
    private volatile static ProxyFactory proxyFactory;

    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    private ProxyFactory() {

    }

    public ProxyFactory classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    /**
     * 设置代理类生成的地方法
     * @param proxyClassName 全限定名
     */
    public ProxyFactory withName(String proxyClassName) {
        proxyContext.setProxyName(proxyClassName);
        return this;
    }

    public <T> T newProxyInstance(Class<T> tClass, InvocationInterceptor interceptor) {
        return proxyContext.proxyInstance(tClass, interceptor);
    }

    public <T> T newProxyInstance(Class<T> tClass, InvocationInterceptor interceptor, boolean slowly) {
        return proxyContext.proxyInstance(classLoader, tClass, interceptor, slowly);
    }

    public <T> T newProxyInstance(Class<T> tClass, InvocationInterceptor interceptor, Object[] args) {
        return proxyContext.proxyInstance(classLoader, tClass, interceptor, false, args);
    }

    public <T> T newProxyInstance(Class<T> tClass, InvocationInterceptor interceptor, boolean slowly, Object[] args) {
        return proxyContext.proxyInstance(classLoader, tClass, interceptor, slowly, args);
    }

    public static ProxyFactory proxy() {
        if (proxyFactory == null) {
            synchronized (ProxyFactory.class) {
                if (proxyFactory == null) {
                    proxyFactory = new ProxyFactory();
                }
            }
        }
        return proxyFactory;
    }
}
