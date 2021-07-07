package com.jw.screw.spring.properties;

import java.util.Properties;

/**
 * 注册中心默认配置
 * @author jiangw
 * @date 2021/6/25 18:06
 * @since 1.0
 */
public class RegistryProperties implements DefaultProperties {

    // registry.port=8501

    @Override
    public Properties get() {
        Properties properties = new Properties();
        properties.put("registry.port", 85011);
        return properties;
    }
}
