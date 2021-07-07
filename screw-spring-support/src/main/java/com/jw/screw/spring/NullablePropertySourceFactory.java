package com.jw.screw.spring;

import com.jw.screw.spring.properties.DefaultPropertiesHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * 可以为空的配置文件
 * @author jiangw
 * @date 2020/12/9 16:53
 * @since 1.0
 */
public class NullablePropertySourceFactory implements PropertySourceFactory {

    private static Logger logger = LoggerFactory.getLogger(NullablePropertySourceFactory.class);

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        PropertiesPropertySource propertySource = null;
        try {
            if (name != null) {
                propertySource = new ResourcePropertySource(name, resource);
            } else {
                propertySource = new ResourcePropertySource(resource);
            }
        } catch (FileNotFoundException e) {
            logger.warn("create {} properties wrong, because: {}", name, e.getMessage());
            DefaultPropertiesHandler handler = new DefaultPropertiesHandler();
            Properties properties = handler.setProperties(e.getMessage()).handle();
            if (properties == null) {
                propertySource = new PropertiesPropertySource("null", new Properties());
            } else {
                logger.info("get default properties is: {}", properties.toString());
                propertySource = new PropertiesPropertySource(name, properties);
            }
        }
        return propertySource;
    }
}
