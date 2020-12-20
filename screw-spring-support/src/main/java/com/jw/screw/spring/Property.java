package com.jw.screw.spring;

import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.util.Requires;
import com.jw.screw.spring.anntation.ScrewValue;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author jiangw
 * @date 2020/12/9 16:55
 * @since 1.0
 */
public class Property {

    private static CopyOnWriteArrayList<TypePropertySource<?>> resourcesPool = new CopyOnWriteArrayList<>();

    public synchronized static void addResource(TypePropertySource<?> resource) {
        resourcesPool.add(resource);
    }

    /**
     * 根据资源名删除资源
     * @param name
     */
    public synchronized static void deleteResource(String name) {
        Requires.isNull(name, "name");
        TypePropertySource<?> resource = null;
        for (TypePropertySource<?> source : resourcesPool) {
            if (source.getName().equals(name)) {
                resource = source;
            }
        }
        deleteResource(resource);
    }

    private synchronized static void deleteResource(TypePropertySource<?> resource) {
        if (resource != null) {
            resourcesPool.remove(resource);
        }
    }

    public synchronized static Object get(String name) {
        TypePropertySource<?> source = lookup(name);
        if (source == null) {
            return null;
        }
        String[] splitPropertiesName = name.split(StringPool.DASH);
        if (splitPropertiesName.length < 2) {
            throw new IllegalArgumentException("name format error");
        }
        String propertiesName = splitPropertiesName[1];
        return source.get(propertiesName);
    }

    public synchronized static String getString(String name) {
        TypePropertySource<?> source = lookup(name);
        if (source == null) {
            return null;
        }
        String[] splitPropertiesName = name.split(StringPool.DASH);
        if (splitPropertiesName.length < 2) {
            throw new IllegalArgumentException("name format error");
        }
        String propertiesName = splitPropertiesName[1];
        return source.getString(propertiesName);
    }

    public synchronized static Integer getInteger(String name) {
        TypePropertySource<?> source = lookup(name);
        if (source == null) {
            return null;
        }
        String[] splitPropertiesName = name.split(StringPool.DASH);
        if (splitPropertiesName.length < 2) {
            throw new IllegalArgumentException("name format error");
        }
        String propertiesName = splitPropertiesName[1];
        return source.getInteger(propertiesName);
    }

    public synchronized static Long getLong(String name) {
        TypePropertySource<?> source = lookup(name);
        if (source == null) {
            return null;
        }
        String[] splitPropertiesName = name.split(StringPool.DASH);
        if (splitPropertiesName.length < 2) {
            throw new IllegalArgumentException("name format error");
        }
        String propertiesName = splitPropertiesName[1];
        return source.getLong(propertiesName);
    }

    public synchronized static Double getDouble(String name) {
        TypePropertySource<?> source = lookup(name);
        if (source == null) {
            return null;
        }
        String[] splitPropertiesName = name.split(StringPool.DASH);
        if (splitPropertiesName.length < 2) {
            throw new IllegalArgumentException("name format error");
        }
        String propertiesName = splitPropertiesName[1];
        return source.getDouble(propertiesName);
    }

    public synchronized static Boolean getBoolean(String name) {
        TypePropertySource<?> source = lookup(name);
        if (source == null) {
            return null;
        }
        String[] splitPropertiesName = name.split(StringPool.DASH);
        if (splitPropertiesName.length < 2) {
            throw new IllegalArgumentException("name format error");
        }
        String propertiesName = splitPropertiesName[1];
        return source.getBoolean(propertiesName);
    }

    public synchronized static Float getFloat(String name) {
        TypePropertySource<?> source = lookup(name);
        if (source == null) {
            return null;
        }
        String[] splitPropertiesName = name.split(StringPool.DASH);
        if (splitPropertiesName.length < 2) {
            throw new IllegalArgumentException("name format error");
        }
        String propertiesName = splitPropertiesName[1];
        return source.getFloat(propertiesName);
    }

    public synchronized static void refreshProperties(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            ScrewValue screwValue = field.getAnnotation(ScrewValue.class);
            if (screwValue == null) {
                continue;
            }
            String propertiesRule = screwValue.value();
            Class<?> type = field.getType();
            try {
                if (type == String.class) {
                    field.set(o, getString(propertiesRule));
                    continue;
                }
                if (type == Integer.class || type == int.class) {
                    field.set(o, getInteger(propertiesRule));
                    continue;
                }
                if (type == Long.class || type == long.class) {
                    field.set(o, getLong(propertiesRule));
                    continue;
                }
                if (type == Double.class || type == double.class) {
                    field.set(o, getDouble(propertiesRule));
                    continue;
                }
                if (type == Boolean.class || type == boolean.class) {
                    field.set(o, getBoolean(propertiesRule));
                    continue;
                }
                if (type == Float.class || type == float.class) {
                    field.set(o, getFloat(propertiesRule));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void synchronous(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
            synchronous(propertySources);
        }
    }

    /**
     * 当从配置中心获取最新的配置后，会把配置放入{@link MutablePropertySources}.此时，需要从刷新资源池中的数据。
     * @param propertySources
     */
    public synchronized static void synchronous(MutablePropertySources propertySources) {
        // 做简单的清除-同步操作
        resourcesPool.clear();
        for (PropertySource<?> source : propertySources) {
            addResource(new TypePropertySource<>(source));
        }
    }

    /**
     * 根据配置的名称查找一个{@link TypePropertySource}
     * 比如：screw-xx 在资源池中查找名称叫作screw的配置
     * @param name
     * @return
     * @throws IllegalArgumentException 如果解析名称或者未找到配置，那么抛出。
     */
    private static TypePropertySource<?> lookup(String name) {
        String[] splitPropertiesName = name.split(StringPool.DASH);
        if (splitPropertiesName.length < 2) {
            throw new IllegalArgumentException("name format error");
        }
        String resourceName = splitPropertiesName[0];
        for (TypePropertySource<?> resource : resourcesPool) {
            if (resource.getName().equals(resourceName)) {
                return resource;
            }
        }
        return null;
    }
}
