package com.jw.screw.spring.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.parser.FormatParser;
import com.jw.screw.common.util.Requires;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.spring.anntation.ScrewValue;
import com.jw.screw.spring.config.hook.ConfigHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.*;
import org.springframework.core.io.support.ResourcePropertySource;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author jiangw
 * @date 2020/12/9 16:55
 * @since 1.0
 */
public class Property {

    private final static Logger LOGGER = LoggerFactory.getLogger(Property.class);

    private static final CopyOnWriteArrayList<TypePropertySource<?>> RESOURCES_POOL = new CopyOnWriteArrayList<>();

    public synchronized static void addResource(TypePropertySource<?> resource) {
        RESOURCES_POOL.add(resource);
    }

    /**
     * 根据资源名删除资源
     * @param name 形如screw-xx的形式
     */
    public synchronized static void deleteResource(String name) {
        Requires.isNull(name, "name");
        TypePropertySource<?> resource = null;
        for (TypePropertySource<?> source : RESOURCES_POOL) {
            if (source.getName().equals(name)) {
                resource = source;
            }
        }
        deleteResource(resource);
    }

    private synchronized static void deleteResource(TypePropertySource<?> resource) {
        if (resource != null) {
            RESOURCES_POOL.remove(resource);
        }
    }

    public synchronized static Object get(String name) {
        PropertyModel propertyModel = getPropertiesName(name);
        try {
            if (propertyModel == null) {
                return null;
            }
            return propertyModel.getSource().get(propertyModel.getPropertiesName());
        } catch (IllegalArgumentException e) {
            LOGGER.info("parse properties error[{}], because, {}", name, e.getMessage());
        }
        return null;
    }

    public synchronized static String getString(String name) {
        PropertyModel propertyModel = getPropertiesName(name);
        try {
            if (propertyModel == null) {
                return null;
            }
            return propertyModel.getSource().getString(propertyModel.getPropertiesName());
        } catch (IllegalArgumentException e) {
            LOGGER.info("parse properties error[{}], because, {}", name, e.getMessage());
        }
        return null;
    }

    public synchronized static Integer getInteger(String name) {
        PropertyModel propertyModel = getPropertiesName(name);
        try {
            if (propertyModel == null) {
                return null;
            }
            return propertyModel.getSource().getInteger(propertyModel.getPropertiesName());
        } catch (IllegalArgumentException e) {
            LOGGER.info("parse properties error[{}], because, {}", name, e.getMessage());
        }
        return null;
    }

    public synchronized static Long getLong(String name) {
        PropertyModel propertyModel = getPropertiesName(name);
        try {
            if (propertyModel == null) {
                return null;
            }
            return propertyModel.getSource().getLong(propertyModel.getPropertiesName());
        } catch (IllegalArgumentException e) {
            LOGGER.info("parse properties error[{}], because, {}", name, e.getMessage());
        }
        return null;
    }

    public synchronized static Double getDouble(String name) {
        PropertyModel propertyModel = getPropertiesName(name);
        try {
            if (propertyModel == null) {
                return null;
            }
            return propertyModel.getSource().getDouble(propertyModel.getPropertiesName());
        } catch (IllegalArgumentException e) {
            LOGGER.info("parse properties error[{}], because, {}", name, e.getMessage());
        }
        return null;
    }

    public synchronized static Boolean getBoolean(String name) {
        PropertyModel propertyModel = getPropertiesName(name);
        try {
            if (propertyModel == null) {
                return null;
            }
            return propertyModel.getSource().getBoolean(propertyModel.getPropertiesName());
        } catch (IllegalArgumentException e) {
            LOGGER.info("parse properties error[{}], because, {}", name, e.getMessage());
        }
        return null;
    }

    public synchronized static Float getFloat(String name) {
        PropertyModel propertyModel = getPropertiesName(name);
        try {
            if (propertyModel == null) {
                return null;
            }
            return propertyModel.getSource().getFloat(propertyModel.getPropertiesName());
        } catch (IllegalArgumentException e) {
            LOGGER.info("parse properties error[{}], because, {}", name, e.getMessage());
        }
        return null;
    }

    private synchronized static PropertyModel getPropertiesName(String name) {
        TypePropertySource<?> source = lookup(name);
        if (source == null) {
            return null;
        }
        String[] splitPropertiesName = name.split(StringPool.DASH);
        if (splitPropertiesName.length < 2) {
            throw new IllegalArgumentException("name format error");
        }
        return new PropertyModel(source, splitPropertiesName[1]);
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
     * @param propertySources {@link MutablePropertySources}
     */
    public synchronized static void synchronous(MutablePropertySources propertySources) {
        // 做简单的清除-同步操作
        RESOURCES_POOL.clear();
        for (PropertySource<?> source : propertySources) {
            addResource(new TypePropertySource<>(source));
        }
    }

    /**
     * 根据配置的名称查找一个{@link TypePropertySource}
     * 比如：screw-xx 在资源池中查找名称叫作screw的配置
     * @param name 形如screw-xx的实行是
     * @return {@link TypePropertySource}
     * @throws IllegalArgumentException 如果解析名称或者未找到配置，那么抛出。
     */
    private static TypePropertySource<?> lookup(String name) {
        String[] splitPropertiesName = name.split(StringPool.DASH);
        if (splitPropertiesName.length < 2) {
            throw new IllegalArgumentException("name format error");
        }
        String resourceName = splitPropertiesName[0];
        for (TypePropertySource<?> resource : RESOURCES_POOL) {
            if (resource.getName().equals(resourceName)) {
                return resource;
            }
        }
        return null;
    }

    public static synchronized void addProperties(PropertiesPropertySource propertySource, Environment environment) {
        addProperties((MapPropertySource) propertySource, environment);
    }

    /**
     * 向spring上下文添加配置
     * @param propertySource {@link PropertiesPropertySource}
     * @param environment {@link Environment}
     */
    public static synchronized void addProperties(MapPropertySource propertySource, Environment environment) {
        String name = propertySource.getName();
        MutablePropertySources propertySources = ((ConfigurableEnvironment) environment).getPropertySources();
        if (propertySources.contains(name)) {
            PropertySource<?> existing = propertySources.get(name);
            PropertySource<?> newSource = (propertySource instanceof ResourcePropertySource ?
                    ((ResourcePropertySource) propertySource).withResourceName() : propertySource);
            if (existing instanceof CompositePropertySource) {
                ((CompositePropertySource) existing).addFirstPropertySource(newSource);
            } else {
                if (existing instanceof ResourcePropertySource) {
                    existing = ((ResourcePropertySource) existing).withResourceName();
                }
                CompositePropertySource composite = new CompositePropertySource(name);
                composite.addPropertySource(newSource);
                composite.addPropertySource(existing);
                propertySources.replace(name, composite);
            }
        } else {
            if (name == null) {
                propertySources.addFirst(propertySource);
            } else {
                propertySources.addLast(propertySource);
            }
        }
    }

    public static void configChanged(JSONObject configObj, ApplicationContext applicationContext) {
        if (!configObj.containsKey("configModel")) {
            return;
        }
        JSONArray configModels = configObj.getJSONArray("configModel");
        for (int i = 0; i < configModels.size(); i++) {
            JSONObject configModel = configModels.getJSONObject(i);
            String configName = configModel.getString("configName");
            String configJson = configModel.getString("configJson");
            Properties properties;
            if (StringUtils.isEmpty(configJson)) {
                properties = new Properties();
            } else {
                properties = ConfigParser.getProperties(configJson);
            }
            PropertiesPropertySource propertySource = new PropertiesPropertySource(configName, properties);
            addProperties(propertySource, applicationContext.getEnvironment());
        }
    }

    public static void initBasicConfig(JSONObject configObj, ApplicationContext applicationContext) {
        boolean isServerPort = configObj.containsKey("serverPort");
        if (!isServerPort) {
            throw new NullPointerException("properties server port is null!");
        }
        Properties serverProperties = new Properties();
        serverProperties.setProperty("server.port", configObj.getString("serverPort"));
        Property.addProperties(new PropertiesPropertySource("server", serverProperties), applicationContext.getEnvironment());
        Properties applicationProperties = new Properties();
        applicationProperties.setProperty("spring.application.name", configObj.getString("serverName"));
        Property.addProperties(new PropertiesPropertySource("spring.application", applicationProperties), applicationContext.getEnvironment());
    }

    public static void initDataSource(JSONArray datasourceArray, ApplicationContext applicationContext) {
        for (int i = 0; i < datasourceArray.size(); i++) {
            JSONObject datasourceModel = datasourceArray.getJSONObject(i);
            String datasourceType = datasourceModel.getString("datasourceType");
            ConfigCenterConnectable.DataTypeEnum dataTypeEnum = ConfigCenterConnectable.DataTypeEnum.typeOf(datasourceModel.getString("datasourceType"));
            if (dataTypeEnum != null) {
                String jdbcUrl = dataTypeEnum.getJdbcUrl()
                        .replace("{ip}", datasourceModel.getString("datasourceIp"))
                        .replace("{port}", datasourceModel.getString("datasourcePort"))
                        .replace("{dbname}", datasourceModel.getString("datasourceConnectName"));
                if (ConfigCenterConnectable.DataTypeEnum.MYSQL.getFeature().equals(datasourceType)) {
                    jdbcUrl = jdbcUrl + "?useUnicode=true&characterEncoding=utf-8&serverTimezone=Hongkong";
                }
                datasourceModel.put("url", jdbcUrl);
                Properties properties = new Properties();
                String datasourceConnectVariables = datasourceModel.getString("datasourceConnectVariables");
                if (StringUtils.isNotEmpty(datasourceConnectVariables)) {
                    String[] variables = FormatParser.jsonToProperties(datasourceConnectVariables).split(StringPool.NEWLINE);
                    for (String variable : variables) {
                        String[] keyValue = variable.split(StringPool.EQUALS);
                        try {
                            properties.setProperty(keyValue[0], keyValue[1]);
                        } catch (IndexOutOfBoundsException e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                }
                datasourceModel.put("datasourceType", ConfigCenterConnectable.DataSourceType.getClassName(datasourceModel.getString("datasourceConnectType")));
                try {
                    ConfigHook hook = applicationContext.getBean(ConfigHook.class);
                    datasourceModel.put("driverClassName", dataTypeEnum.getDriver());
                    hook.dataSourceLoader(datasourceModel, properties, applicationContext);
                } catch (Exception e) {
                    properties.setProperty("spring.datasource.url", jdbcUrl);
                    properties.setProperty("spring.datasource.username", datasourceModel.getString("datasourceUsername"));
                    properties.setProperty("spring.datasource.password", datasourceModel.getString("datasourcePassword"));
                    properties.setProperty("spring.datasource.driver-class-name", dataTypeEnum.getDriver());
                    properties.setProperty("spring.datasource.type", datasourceModel.getString("datasourceType"));
                    PropertiesPropertySource propertySource = new PropertiesPropertySource("spring.datasource", properties);
                    Property.addProperties(propertySource, applicationContext.getEnvironment());
                }
            }
        }
    }

    static class PropertyModel {

        private final TypePropertySource<?> source;

        private final String propertiesName;

        public PropertyModel(TypePropertySource<?> source, String propertiesName) {
            this.source = source;
            this.propertiesName = propertiesName;
        }

        public TypePropertySource<?> getSource() {
            return source;
        }

        public String getPropertiesName() {
            return propertiesName;
        }
    }
}
