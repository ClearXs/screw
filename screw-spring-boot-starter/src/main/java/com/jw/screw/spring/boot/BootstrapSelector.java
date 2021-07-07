package com.jw.screw.spring.boot;

import com.jw.screw.common.util.StringUtils;
import com.jw.screw.spring.config.Property;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author jiangw
 * @date 2021/1/8 16:37
 * @since 1.0
 */
public class BootstrapSelector implements ImportSelector, Ordered, EnvironmentAware {

    private static Environment environment;

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        List<String> importPackages = new ArrayList<>();
        Set<String> annotationTypes = metadata.getAnnotationTypes();
        for (String annotationType : annotationTypes) {
            EnableMapping mapping = EnableMapping.select(annotationType);
            if (mapping != null) {
                // 解析注解并放入spring配置中
                AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                        metadata.getAnnotationAttributes(mapping.getEnableAnnotation(), true));
                mapping.importProperty(attributes);
                String importClass = mapping.getImportClass();
                if (mapping == EnableMapping.PROVIDER && attributes != null) {
                    boolean isConfigCenter = attributes.getBoolean("isConfigCenter");
                    if (isConfigCenter) {
                        importClass = com.jw.screw.spring.boot.ScrewConfigConfiguration.class.getName();
                    }
                }
                if (mapping == EnableMapping.CONSUMER) {
                    importPackages.add(com.jw.screw.spring.boot.BootRefreshContext.class.getName());
                }
                importPackages.add(importClass);
            }
        }
        return importPackages.toArray(new String[] {});
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    @Override
    public void setEnvironment(Environment environment) {
        BootstrapSelector.environment = environment;
    }

    /**
     * 注解与对应配置类的映射关系
     */
    enum EnableMapping {

        /**
         * consumer映射
         */
        CONSUMER(com.jw.screw.spring.boot.EnableScrewConsumer.class.getName(), com.jw.screw.spring.boot.ScrewConsumerConfiguration.class.getName()),

        /**
         * provider映射
         */
        PROVIDER(com.jw.screw.spring.boot.EnableScrewProvider.class.getName(), com.jw.screw.spring.boot.ScrewProviderConfiguration.class.getName()),

        /**
         * registry映射
         */
        REGISTRY(com.jw.screw.spring.boot.EnableScrewRegistry.class.getName(), com.jw.screw.spring.boot.ScrewRegistryConfiguration.class.getName()),

        /**
         * monitor映射
         */
        MONITOR(com.jw.screw.spring.boot.EnableScrewMonitor.class.getName(), com.jw.screw.spring.boot.ScrewMonitorConfiguration.class.getName());

        /**
         * 启用的注解
         */
        private final String enableAnnotation;

        /**
         * 导入的类
         */
        private final String importClass;

        EnableMapping(String enableAnnotation, String importClass) {
            this.enableAnnotation = enableAnnotation;
            this.importClass = importClass;
        }

        public String getEnableAnnotation() {
            return enableAnnotation;
        }

        public String getImportClass() {
            return importClass;
        }

        public static EnableMapping select(String enableAnnotation) {
            if (StringUtils.isEmpty(enableAnnotation)) {
                return null;
            }
            for (EnableMapping value : values()) {
                if (value.getEnableAnnotation().equals(enableAnnotation)) {
                    return value;
                }
            }
            return null;
        }

        /**
         * 根据注解的配置导入到当前spring {@link Environment}
         * @param attributes {@link AnnotationAttributes}
         */
        public void importProperty(AnnotationAttributes attributes) {
            PropertiesPropertySource propertySource = null;
            Properties properties = new Properties();
            ScrewPropertyModel propertyModel = ScrewPropertyModel.getInstance(environment, attributes);
            if (this == CONSUMER) {
                properties.setProperty("enable", propertyModel.getEnable());
                properties.setProperty("server.key", propertyModel.getServerKey());
                properties.setProperty("provider.server.port", propertyModel.getServerPort());
                properties.setProperty("registry.address", propertyModel.getRegistryAddress());
                properties.setProperty("registry.port", propertyModel.getRegistryPort());
                properties.setProperty("waitMills", propertyModel.getWaitMills());
                properties.setProperty("loadbalance", propertyModel.getLoadbalance());
                properties.setProperty("config.key", propertyModel.getConfigKey());
                properties.setProperty("monitor.key", propertyModel.getMonitorKey());
                properties.setProperty("monitor.collect.period", propertyModel.getMonitorCollectPeriod());
                propertySource = new PropertiesPropertySource("consumer", properties);
            } else if (this == PROVIDER) {
                properties.setProperty("enable", propertyModel.getEnable());
                properties.setProperty("server.key", propertyModel.getServerKey());
                properties.setProperty("provider.address", propertyModel.getProviderAddress());
                properties.setProperty("provider.server.port", propertyModel.getServerPort());
                properties.setProperty("registry.address", propertyModel.getRegistryAddress());
                properties.setProperty("registry.port", propertyModel.getRegistryPort());
                properties.setProperty("weight", propertyModel.getWeight());
                properties.setProperty("connCount", propertyModel.getConnCount());
                properties.setProperty("monitor.key", propertyModel.getMonitorKey());
                properties.setProperty("monitor.collect.period", propertyModel.getMonitorCollectPeriod());
                properties.setProperty("packageScan", propertyModel.getPackageScan());
                propertySource = new PropertiesPropertySource("provider", properties);
            } else if (this == REGISTRY) {
                properties.setProperty("registry.port", propertyModel.getRegistryPort());
                propertySource = new PropertiesPropertySource("registry", properties);
            } else if (this == MONITOR) {
                properties.setProperty("registry.address", propertyModel.getRegistryAddress());
                properties.setProperty("registry.port", propertyModel.getRegistryPort());
                properties.setProperty("monitor.address", propertyModel.getMonitorAddress());
                properties.setProperty("weight", propertyModel.getWeight());
                properties.setProperty("connCount", propertyModel.getConnCount());
                propertySource = new PropertiesPropertySource("monitor", properties);
            }
            if (propertySource != null) {
                Property.addProperties(propertySource, environment);
                Property.synchronous(environment);
            }
        }
    }

    /**
     * property的model，剔出作为公共类
     */
    static class ScrewPropertyModel {

        private String enable;

        /**
         * 服务key
         */
        private String serverKey;

        /**
         * 服务地址
         */
        private String providerAddress;

        /**
         * 服务port
         */
        private String serverPort;

        /**
         * 注册中心地址
         */
        private String registryAddress;

        /**
         * 注册中心端口
         */
        private String registryPort;

        /**
         * 等待时长
         */
        private String waitMills;

        /**
         * 负载均衡
         */
        private String loadbalance;

        /**
         * 配置中心key
         */
        private String configKey;

        /**
         * 监控中心key
         */
        private String monitorKey;

        /**
         * 监控地址
         */
        private String monitorAddress;

        /**
         * 监控指标周期
         */
        private String monitorCollectPeriod;

        /**
         * 包扫描
         */
        private String packageScan;

        /**
         * 服务权重
         */
        private String weight;

        /**
         * 连接数量
         */
        private String connCount;

        public String getEnable() {
            return enable;
        }

        public void setEnable(String enable) {
            this.enable = enable;
        }

        public String getServerKey() {
            return serverKey;
        }

        public void setServerKey(String serverKey) {
            this.serverKey = serverKey;
        }

        public String getServerPort() {
            return serverPort;
        }

        public void setServerPort(String serverPort) {
            this.serverPort = serverPort;
        }

        public String getRegistryAddress() {
            return registryAddress;
        }

        public void setRegistryAddress(String registryAddress) {
            this.registryAddress = registryAddress;
        }

        public String getRegistryPort() {
            return registryPort;
        }

        public void setRegistryPort(String registryPort) {
            this.registryPort = registryPort;
        }

        public String getWaitMills() {
            return waitMills;
        }

        public void setWaitMills(String waitMills) {
            this.waitMills = waitMills;
        }

        public String getLoadbalance() {
            return loadbalance;
        }

        public void setLoadbalance(String loadbalance) {
            this.loadbalance = loadbalance;
        }

        public String getConfigKey() {
            return configKey;
        }

        public void setConfigKey(String configKey) {
            this.configKey = configKey;
        }

        public String getMonitorKey() {
            return monitorKey;
        }

        public void setMonitorKey(String monitorKey) {
            this.monitorKey = monitorKey;
        }

        public String getMonitorCollectPeriod() {
            return monitorCollectPeriod;
        }

        public void setMonitorCollectPeriod(String monitorCollectPeriod) {
            this.monitorCollectPeriod = monitorCollectPeriod;
        }

        public String getPackageScan() {
            return packageScan;
        }

        public void setPackageScan(String packageScan) {
            this.packageScan = packageScan;
        }

        public String getWeight() {
            return weight;
        }

        public void setWeight(String weight) {
            this.weight = weight;
        }

        public String getConnCount() {
            return connCount;
        }

        public void setConnCount(String connCount) {
            this.connCount = connCount;
        }

        public String getProviderAddress() {
            return providerAddress;
        }

        public void setProviderAddress(String providerAddress) {
            this.providerAddress = providerAddress;
        }

        public String getMonitorAddress() {
            return monitorAddress;
        }

        public void setMonitorAddress(String monitorAddress) {
            this.monitorAddress = monitorAddress;
        }

        public static synchronized ScrewPropertyModel getInstance(Environment environment, AnnotationAttributes attributes) {
            ScrewPropertyModel propertyModel = new ScrewPropertyModel();

            String enable = "";
            try {
                enable = String.valueOf(attributes.getBoolean("enable"));
            } catch (IllegalArgumentException e) {
                enable = "";
            }
            propertyModel.setEnable(enable);


            String serverKey = "";
            try {
                serverKey = attributes.getString("serverKey");
            } catch (IllegalArgumentException e) {
                serverKey = "";
            }
            propertyModel.setServerKey(serverKey);

            String serverPort = environment.getProperty("provider.server.port");
            if (StringUtils.isEmpty(serverPort)) {
                try {
                    serverPort = String.valueOf((Integer) attributes.get("serverPort"));
                } catch (IllegalArgumentException e) {
                    serverPort = "";
                }
            }
            propertyModel.setServerPort(serverPort);

            String registryAddress = environment.getProperty("registry.address");
            if (StringUtils.isEmpty(registryAddress)) {
                try {
                    registryAddress = attributes.getString("registryAddress");
                } catch (IllegalArgumentException e) {
                    registryAddress = "";
                }
            }
            propertyModel.setRegistryAddress(registryAddress);

            String registryPort = environment.getProperty("registry.port");
            if (StringUtils.isEmpty(registryPort)) {
                try {
                    registryPort = String.valueOf((Integer) attributes.get("registryPort"));
                } catch (IllegalArgumentException e) {
                    registryPort = "";
                }
            }
            propertyModel.setRegistryPort(registryPort);

            String waitMills = environment.getProperty("waitMills");
            if (StringUtils.isEmpty(waitMills)) {
                try {
                    waitMills = String.valueOf((Long) attributes.get("waitMills"));
                } catch (IllegalArgumentException e) {
                    waitMills = "";
                }
            }
            propertyModel.setWaitMills(waitMills);

            String loadbalance = environment.getProperty("loadbalance");
            if (StringUtils.isEmpty(loadbalance)) {
                try {
                    loadbalance = attributes.getString("loadBalance");
                } catch (IllegalArgumentException e) {
                    loadbalance = "";
                }
            }
            propertyModel.setLoadbalance(loadbalance);

            String configKey = environment.getProperty("config.key");
            if (StringUtils.isEmpty(configKey)) {
                try {
                    configKey = attributes.getString("configKey");
                } catch (IllegalArgumentException e) {
                    configKey = "";
                }
            }
            propertyModel.setConfigKey(configKey);

            String monitorKey = environment.getProperty("monitor.key");
            if (StringUtils.isEmpty(monitorKey)) {
                try {
                    monitorKey = attributes.getString("monitorKey");
                } catch (IllegalArgumentException e) {
                    monitorKey = "";
                }
            }
            propertyModel.setMonitorKey(monitorKey);

            String monitorCollectPeriod = environment.getProperty("monitor.collect.period");
            if (StringUtils.isEmpty(monitorCollectPeriod)) {
                try {
                    monitorCollectPeriod = String.valueOf((Integer) attributes.get("monitorCollectPeriod"));
                } catch (IllegalArgumentException e) {
                    monitorCollectPeriod = "";
                }
            }
            propertyModel.setMonitorCollectPeriod(monitorCollectPeriod);

            String packageScan = environment.getProperty("packageScan");
            if (StringUtils.isEmpty(packageScan)) {
                try {
                    packageScan = attributes.getString("packageScan");
                } catch (IllegalArgumentException e) {
                    packageScan = "";
                }
            }
            propertyModel.setPackageScan(packageScan);

            String weight = environment.getProperty("weight");
            if (StringUtils.isEmpty(weight)) {
                try {
                    weight = String.valueOf((Integer) attributes.get("weight"));
                } catch (IllegalArgumentException e) {
                    weight = "";
                }
            }
            propertyModel.setWeight(weight);

            String connCount = environment.getProperty("connCount");
            if (StringUtils.isEmpty(connCount)) {
                try {
                    connCount = String.valueOf((Integer) attributes.get("connCount"));
                } catch (IllegalArgumentException e) {
                    connCount = "";
                }
            }
            propertyModel.setConnCount(connCount);

            String providerAddress = environment.getProperty("provider.address");
            if (StringUtils.isEmpty(providerAddress)) {
                try {
                    providerAddress = attributes.getString("providerAddress");
                } catch (IllegalArgumentException e) {
                    providerAddress = "";
                }
            }
            propertyModel.setProviderAddress(providerAddress);

            String monitorAddress = environment.getProperty("monitor.address");
            if (StringUtils.isEmpty(monitorAddress)) {
                try {
                    monitorAddress = attributes.getString("monitorAddress");
                } catch (IllegalArgumentException e) {
                    monitorAddress = "";
                }
            }
            propertyModel.setMonitorAddress(monitorAddress);
            return propertyModel;
        }
    }
}
