package com.jw.screw.spring.properties;

import org.springframework.util.StringUtils;

import java.util.Properties;

public class DefaultPropertiesHandler {

    // 以下常量的值为框架定死，如果产生变化将会获取不到对应的
    // 配置

    private final static String CONSUMER_PROPERTIES = "screw-consumer.properties";

    private final static String PROVIDER_PROPERTIES = "screw-provider.properties";

    private final static String REGISTER_PROPERTIES = "screw-registry.properties";

    private final static String MONITOR_PROPERTIES = "screw-monitor.properties";

    private DefaultProperties properties;

    public DefaultPropertiesHandler setProperties(String prop) {
        if (StringUtils.isEmpty(prop)) {
            return this;
        }
        if (prop.contains(CONSUMER_PROPERTIES)) {
            this.properties = new ConsumerProperties();
        } else if (prop.contains(PROVIDER_PROPERTIES)) {
            this.properties = new ProviderProperties();
        } else if (prop.contains(REGISTER_PROPERTIES)) {
            this.properties = new RegistryProperties();
        } else if (prop.contains(MONITOR_PROPERTIES)) {
            this.properties = new MonitorProperties();
        }
        return this;
    }

    public Properties handle() {
        if (properties != null) {
            return properties.get();
        }
        return null;
    }
}
