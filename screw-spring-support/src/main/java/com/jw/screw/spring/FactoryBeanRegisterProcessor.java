package com.jw.screw.spring;

import com.jw.screw.common.util.Collections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.Map;

/**
 * @author jiangw
 */
public class FactoryBeanRegisterProcessor implements BeanDefinitionRegistryPostProcessor {

    private Map<Class<?>, ProxyHandler> proxies;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (Collections.isNotEmpty(proxies)) {
            for (Map.Entry<Class<?>, ProxyHandler> proxy : proxies.entrySet()) {
                Class<?> proxyClass = proxy.getKey();
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(proxyClass);
                GenericBeanDefinition beanDefinition = (GenericBeanDefinition) builder.getRawBeanDefinition();
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(proxyClass);
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(proxy.getValue());
                // 工厂bean
                beanDefinition.setBeanClass(ScrewSpringConsumer.ConsumerBean.class);
                // byType注入
                beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                registry.registerBeanDefinition(proxyClass.getSimpleName(), beanDefinition);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    public void setProxies(Map<Class<?>, ProxyHandler> proxies) {
        this.proxies = proxies;
    }
}
