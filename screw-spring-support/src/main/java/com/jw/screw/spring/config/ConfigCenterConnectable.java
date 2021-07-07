package com.jw.screw.spring.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.future.FutureListener;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.consumer.ConnectionWatcher;
import com.jw.screw.consumer.NettyConsumer;
import com.jw.screw.consumer.RepeatableFuture;
import com.jw.screw.consumer.model.ProxyObjectFactory;
import com.jw.screw.remote.netty.config.GlobeConfig;
import com.jw.screw.spring.event.PropertiesRefreshEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.IOException;
import java.util.Properties;

/**
 * 配置中心的连接操作
 * @author jiangw
 * @date 2021/1/4 16:31
 * @since 1.0
 */
public class ConfigCenterConnectable {

    private static Logger logger = LoggerFactory.getLogger(ConfigCenterConnectable.class);

    private static volatile ConfigCenterConnectable connectable;

    private final ApplicationContext applicationContext;

    public ConfigCenterConnectable(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public synchronized void configCenter(String configServerKey, NettyConsumer nettyConsumer) throws RemoteTimeoutException, ConnectionException, InterruptedException {
        try {
            ServiceMetadata configCenter = new ServiceMetadata(configServerKey);
            ConnectionWatcher connectionWatcher = nettyConsumer.watchConnect(configCenter);
            boolean available = connectionWatcher.waitForAvailable(GlobeConfig.CONNECT_TIMEOUT_MILLIS);
            if (!available) {
                throw new RemoteTimeoutException("unable to connect config center, because: timeout");
            }
            // 初始化查询配置文件
            String config = (String) ProxyObjectFactory
                    .factory()
                    .isAsync(false)
                    .consumer(nettyConsumer)
                    .metadata(configCenter)
                    .connectWatch(connectionWatcher)
                    .remoteInvoke("RemoteConfigService", "queryConfigByServerCodeToJson", new String[] { nettyConsumer.getConfig().getServerKey() });
            initConfig(config);
            listenerConfigAdd(configServerKey);
            listenerConfigDelete(configServerKey);
            listenerConfigUpdate(configServerKey);
        } catch (IllegalArgumentException | InterruptedException | ConnectionException | IOException e) {
            logger.warn("unable to connect config center, because: {}", e.getMessage());
        }
    }

    private void initConfig(String config) throws IOException {
        if (StringUtils.isNotEmpty(config)) {
            JSONObject configObj = JSONObject.parseObject(config);
            // 1.服务基本配置，如端口 服务名（基于spring boot）
            Property.initBasicConfig(configObj, applicationContext);
            // 2.数据源（基于{"datasourceConnectName":"test","datasourceIp":"localhost","datasourceName":"mysql_test","datasourcePassword":"123456","datasourcePort":"3306","datasourceType":"mysql","datasourceUsername":"root"} 结构）
            boolean isDataSource = configObj.containsKey("datasourceModel");
            if (isDataSource) {
                JSONArray datasourceModel = configObj.getJSONArray("datasourceModel");
                Property.initDataSource(datasourceModel, applicationContext);
            }
            // 3.其他配置
            Property.configChanged(configObj, applicationContext);
            // 同步缓存中的数据
            Property.synchronous(applicationContext.getEnvironment());
            // 通知刷新配置数据
            applicationContext.publishEvent(new PropertiesRefreshEvent(applicationContext));
            // 输出为配置文件
            ConfigFile configFile = ConfigFile.newInstance();
            configFile.writeConfig(config);
        }
    }

    /**
     * 监听配置添加
     * @param configServerKey 配置中心server key
     */
    public void listenerConfigAdd(String configServerKey) {
        RepeatableFuture<String> addFuture = ConfigListeners.onAdd(configServerKey);
        addFuture.addListener(new FutureListener<String>() {
            @Override
            public void completed(String result, Throwable throwable) throws Exception {
                if (StringUtils.isNotEmpty(result)) {
                    JSONObject configObj = JSONObject.parseObject(result);
                    String serverCode = configObj.getString("serverCode");
                    if (!serverCode.equals(configServerKey)) {
                        return;
                    }
                    Property.configChanged(configObj, applicationContext);
                    // 同步缓存中的数据
                    Property.synchronous(applicationContext.getEnvironment());
                    // 通知刷新配置数据
                    applicationContext.publishEvent(new PropertiesRefreshEvent(applicationContext));
                    // 输出为配置文件
                    ConfigFile configFile = ConfigFile.newInstance();
                    configFile.writeConfig(result);
                }
            }
        });
    }

    /**
     * 监听配置的删除
     * @param configServerKey 配置中心的server key
     */
    public void listenerConfigDelete(String configServerKey) {
        RepeatableFuture<String> deleteFuture = ConfigListeners.onDelete(configServerKey);
        deleteFuture.addListener(new FutureListener<String>() {
            @Override
            public void completed(String result, Throwable throwable) throws Exception {
                if (StringUtils.isNotEmpty(result)) {
                    JSONObject configObj = JSONObject.parseObject(result);
                    if (!configObj.containsKey("configModel")) {
                        return;
                    }
                    JSONArray configModels = configObj.getJSONArray("configModel");
                    for (int i = 0; i < configModels.size(); i++) {
                        JSONObject configModel = configModels.getJSONObject(i);
                        String configName = configModel.getString("configName");
                        Environment environment = applicationContext.getEnvironment();
                        if (environment instanceof ConfigurableEnvironment) {
                            ((ConfigurableEnvironment) environment).getPropertySources().remove(configName);
                        }
                    }
                    // 同步缓存中的数据
                    Property.synchronous(applicationContext.getEnvironment());
                    // 通知刷新配置数据
                    applicationContext.publishEvent(new PropertiesRefreshEvent(applicationContext));
                    // 输出为配置文件
                    ConfigFile configFile = ConfigFile.newInstance();
                    configFile.writeConfig(result);
                }
            }
        });
    }

    /**
     * 监听配置更新
     * @param configServerKey 配置中心的server key
     */
    public void listenerConfigUpdate(String configServerKey) {
        RepeatableFuture<String> updateFuture = ConfigListeners.onUpdate(configServerKey);
        updateFuture.addListener(new FutureListener<String>() {
            @Override
            public void completed(String result, Throwable throwable) throws Exception {
                if (StringUtils.isNotEmpty(result)) {
                    JSONObject configObj = JSONObject.parseObject(result);
                    if (!configObj.containsKey("configModel")) {
                        return;
                    }
                    JSONArray configModels = configObj.getJSONArray("configModel");
                    for (int i = 0; i < configModels.size(); i++) {
                        JSONObject configModel = configModels.getJSONObject(i);
                        String configName = configModel.getString("configName");
                        Environment environment = applicationContext.getEnvironment();
                        if (environment instanceof ConfigurableEnvironment) {
                            ((ConfigurableEnvironment) environment).getPropertySources().remove(configName);
                        }
                        String configJson = configModel.getString("configJson");
                        Properties properties;
                        if (StringUtils.isEmpty(configJson)) {
                            properties = new Properties();
                        } else {
                            properties = ConfigParser.getProperties(configJson);
                        }
                        PropertiesPropertySource propertySource = new PropertiesPropertySource(configName, properties);
                        Property.addProperties(propertySource, applicationContext.getEnvironment());
                    }
                    // 同步缓存中的数据
                    Property.synchronous(applicationContext.getEnvironment());
                    // 通知刷新配置数据
                    applicationContext.publishEvent(new PropertiesRefreshEvent(applicationContext));
                    // 输出为配置文件
                    ConfigFile configFile = ConfigFile.newInstance();
                    configFile.writeConfig(result);
                }
            }
        });
    }

    public static ConfigCenterConnectable getInstance(ApplicationContext applicationContext) {
        if (connectable == null) {
            synchronized (ConfigCenterConnectable.class) {
                if (connectable == null) {
                    connectable = new ConfigCenterConnectable(applicationContext);
                }
            }
        }
        return connectable;
    }

    enum DataTypeEnum {

        /**
         * mysql
         */
        MYSQL("mysql", "mysql", "com.mysql.jdbc.Driver","jdbc:mysql://{ip}:{port}/{dbname}"),

        /**
         * oracle
         */
        ORACLE("oracle", "oracle", "oracle.jdbc.driver.OracleDriver","jdbc:oracle:thin:@{ip}:{port}:{dbname}"),

        /**
         * sql server
         */
        SQLSERVER("mssql", "mssql", "com.microsoft.sqlserver.jdbc.SQLServerDriver","jdbc:sqlserver://{ip}:{port};DatabaseName={dbname}");

        private final String feature;
        private final String desc;
        private final String driver;
        private final String jdbcUrl;

        DataTypeEnum(String feature, String desc, String driver,String jdbcUrl) {
            this.feature = feature;
            this.desc = desc;
            this.driver = driver;
            this.jdbcUrl = jdbcUrl;
        }

        public static DataTypeEnum typeOf(String feature) {
            for (DataTypeEnum dataTypeEnum : values()) {
                if (dataTypeEnum.feature.equalsIgnoreCase(feature)) {
                    try {
                        Class.forName(dataTypeEnum.getDriver());
                    } catch (ClassNotFoundException e) {
                        logger.warn("Unable to get driver instance: {}" + e.getMessage());
                    }
                    return dataTypeEnum;
                }
            }
            return null;
        }

        public String getFeature() {
            return feature;
        }

        public String getDesc() {
            return desc;
        }

        public String getDriver() {
            return driver;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }
    }

    enum DataSourceType {
        /**
         * spring boot 使用的默认数据源 HikariCP
         */
        DEFAULT("default", "com.zaxxer.hikari.HikariDataSource"),

        /**
         * druid
         */
        DRUID("druid", "com.alibaba.druid.pool.DruidDataSource"),

        /**
         * c3po
         */
        C3P0("c3p0", "com.mchange.v2.c3p0.ComboPooledDataSource"),

        /**
         * dbcp
         */
        DBCP("dbcp", "org.apache.commons.dbcp.BasicDataSource ");

        private final String name;

        private final String className;

        DataSourceType(String name, String className) {
            this.name = name;
            this.className = className;
        }

        public String getName() {
            return name;
        }

        public String getClassName() {
            return className;
        }

        public static String getClassName(String name) {
            if (StringUtils.isEmpty(name)) {
                return "";
            }
            for (DataSourceType dataSourceType : values()) {
                if (dataSourceType.getName().equals(name)) {
                    return dataSourceType.getClassName();
                }
            }
            return "";
        }
    }
}
