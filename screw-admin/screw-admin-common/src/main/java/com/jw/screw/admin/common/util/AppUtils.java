package com.jw.screw.admin.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * spring获取bean properties等工具类
 * @author jiangw
 * @date 2020/8/1 23:26
 * @since 1.0
 */
@Component
public class AppUtils implements ApplicationContextAware {

    private static ApplicationContext app;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        app = applicationContext;
    }

    /**
     * 根据bean名称查找bean
     * @author jiangw
     * @param beanName bean名称
     * @return 返回在spring容器查找到的bean
     * @date 2020/8/1 23:38
     */
    public static Object getBean(String beanName) {
        return app.getBean(beanName);
    }

    /**
     * 根据bean类型查找bean
     * @author jiangw
     * @param tClass bean类型
     * @return 返回在spring容器查找到的bean
     * @date 2020/8/1 23:39
     */
    public static <T> T getBean(Class<T> tClass) {
        return app.getBean(tClass);
    }

    /**
     * 获取属性值
     * @author jiangw
     * @param key 属性名
     * @return 返回属性值
     * @date 2020/8/1 23:38
     */
    public static String getProperty(String key) {
        return getProperty(key, String.class, null);
    }

    /**
     * @author jiangw
     * @param key
     * @param targetType
     * @return
     * @date 2020/8/1 23:41
     */
    public static <T> T getProperty(String key, Class<T> targetType) {
        return getProperty(key, targetType, null);
    }

    /**
     * @author jiangw
     * @param key
     * @param targetType
     * @param defaultValue
     * @return
     * @date 2020/8/1 23:41
     */
    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        Environment environment = app.getEnvironment();
        return environment.getProperty(key, targetType, defaultValue);
    }

    public static Object getProperty(Class<?> targetType) {
        return getBean(targetType);
    }

    public static ApplicationContext getApp() {
        return app;
    }
}
