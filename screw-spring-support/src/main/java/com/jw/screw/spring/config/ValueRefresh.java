package com.jw.screw.spring.config;

/**
 * {@link org.springframework.beans.factory.annotation.Value}的刷新
 * @author jiangw
 * @date 2021/1/8 11:12
 * @since 1.0
 */
public interface ValueRefresh {

    /**
     * 当配置改变时，进行@Value的刷新，其内部实现是基于spring event模型，并且通过内部api实现
     * @param bean spring容器中的bean
     * @param beanName bean名称
     */
    void refresh(Object bean, String beanName);
}
