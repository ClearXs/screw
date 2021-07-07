package com.jw.screw.spring.event;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jw.screw.common.util.Collections;
import com.jw.screw.spring.config.ConfigFile;
import com.jw.screw.spring.config.Property;
import com.jw.screw.spring.config.ValueRefresh;
import com.jw.screw.spring.config.ValueRefreshContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.List;

/**
 * 一个配置的监听类
 * @author jiangw
 * @date 2021/4/16 19:09
 * @since 1.0
 */
public class ConfigListener {

    private static Logger logger = LoggerFactory.getLogger(ConfigListener.class);

    public static void rejectConnection(ApplicationContext applicationContext) {
        try {
            // 1.读取本地配置
            ConfigFile configFile = ConfigFile.newInstance();
            String config = configFile.readConfig();
            // 2.刷新spring环境
            JSONObject configObj = JSONObject.parseObject(config);
            Property.initBasicConfig(configObj, applicationContext);
            Property.configChanged(configObj, applicationContext);
            boolean isDataSource = configObj.containsKey("datasourceModel");
            if (isDataSource) {
                JSONArray datasourceModel = configObj.getJSONArray("datasourceModel");
                Property.initDataSource(datasourceModel, applicationContext);
            }
            // 同步缓存中的数据
            Property.synchronous(applicationContext.getEnvironment());
        } catch (IOException e) {
            logger.error("config file not found: {}", e.getMessage());
        }
    }

    public static void refreshConfig(ApplicationContext applicationContext) {
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = null;
            try {
                bean = applicationContext.getBean(name);
            } catch (Exception e) {
                logger.warn("get bean {} error: {}", name, e.getMessage());
            }
            if (bean == null) {
                continue;
            }
            ValueRefreshContext refreshContext = applicationContext.getBean(ValueRefreshContext.class);
            List<ValueRefresh> refreshes = refreshContext.list();
            if (Collections.isNotEmpty(refreshes)) {
                for (ValueRefresh refresh : refreshes) {
                    try {
                        refresh.refresh(bean, name);
                    } catch (Exception e) {
                        logger.warn("can't refresh [{}], cause: {}", name, e.getMessage());
                    }
                }
            }
            Property.refreshProperties(bean);
        }
    }
}
