package com.jw.screw.spring;

import com.jw.screw.common.util.StringUtils;
import org.springframework.core.env.PropertySource;

/**
 * @author jiangw
 */
public class TypePropertySource<T> {

    private final PropertySource<T> propertySource;

    public TypePropertySource(PropertySource<T> propertySource) {
        this.propertySource = propertySource;
    }

    public String getName() {
        return propertySource.getName();
    }

    public T getSource() {
        return propertySource.getSource();
    }

    public Object get(String name) {
        return propertySource.getProperty(name);
    }

    public String getString(String name) {
        return (String) get(name);
    }

    public Integer getInteger(String name) {
        String string = getString(name);
        if (StringUtils.isEmpty(string)) {
            throw new IllegalArgumentException(name + " property empty");
        }
        return Integer.parseInt(string);
    }

    public Long getLong(String name) {
        String string = getString(name);
        if (StringUtils.isEmpty(string)) {
            throw new IllegalArgumentException(name + " property empty");
        }
        return Long.parseLong(string);
    }

    public Double getDouble(String name) {
        String string = getString(name);
        if (StringUtils.isEmpty(string)) {
            throw new IllegalArgumentException(name + " property empty");
        }
        return Double.parseDouble(string);
    }

    public Boolean getBoolean(String name) {
        String string = getString(name);
        if (StringUtils.isEmpty(string)) {
            throw new IllegalArgumentException(name + " property empty");
        }
        return Boolean.parseBoolean(string);
    }

    public Float getFloat(String name) {
        String string = getString(name);
        if (StringUtils.isEmpty(string)) {
            throw new IllegalArgumentException(name + " property empty");
        }
        return Float.parseFloat(string);
    }

}

