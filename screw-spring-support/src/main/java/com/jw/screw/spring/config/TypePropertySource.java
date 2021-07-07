package com.jw.screw.spring.config;

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
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return propertySource.getProperty(name);
    }

    public String getString(String name) {
        Object o = get(name);
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            return (String) o;
        } else {
            return null;
        }
    }

    public Integer getInteger(String name) {
        Object o = get(name);
        if (o == null) {
            return null;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        } else {
            if (o instanceof String) {
                return Integer.parseInt((String) o);
            }
            return null;
        }
    }

    public Long getLong(String name) {
        Object o = get(name);
        if (o == null) {
            return null;
        }
        if (o instanceof Long) {
            return (Long) o;
        } else {
            if (o instanceof String) {
                return Long.parseLong((String) o);
            }
            return null;
        }
    }

    public Double getDouble(String name) {
        Object o = get(name);
        if (o == null) {
            return null;
        }
        if (o instanceof Double) {
            return (Double) o;
        } else {
            if (o instanceof String) {
                return Double.parseDouble((String) o);
            }
            return null;
        }
    }

    public Boolean getBoolean(String name) {
        Object o = get(name);
        if (o == null) {
            return null;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else {
            if (o instanceof String) {
                return Boolean.getBoolean((String) o);
            }
            return null;
        }
    }

    public Float getFloat(String name) {
        Object o = get(name);
        if (o == null) {
            return null;
        }
        if (o instanceof Float) {
            return (Float) o;
        } else {
            if (o instanceof String) {
                return Float.parseFloat((String) o);
            }
            return null;
        }
    }

}

