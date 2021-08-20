package com.jw.screw.common.proxy;

/**
 * 代理策略
 * @author jiangw
 * @date 2021/8/12 16:33
 * @since 1.0
 */
interface ProxyInvocation {

    /**
     * 创建实例
     * @param classLoader 生成目标对象class文件的类加载器
     * @param target 需要代理的class对象
     * @param interceptor 统一方法拦截器
     * @param <T> 目标对象类型
     * @return 目标对象类型实例
     */
    <T> T proxyInstance(ClassLoader classLoader, Class<T> target, InvocationInterceptor interceptor, Object[] args) throws InstantiationException, IllegalAccessException;

    /**
     * 代理类全限定名称
     */
    default void proxyName(String proxyName) {

    };
}
