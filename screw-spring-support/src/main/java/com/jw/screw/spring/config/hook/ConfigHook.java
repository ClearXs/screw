package com.jw.screw.spring.config.hook;

import com.alibaba.fastjson.JSONObject;
import org.springframework.context.ApplicationContext;

import java.util.Properties;

/**
 * 配置相关的钩子
 * @author jiangw
 * @date 2021/1/25 14:57
 * @since 1.0
 */
public interface ConfigHook {

    /**
     * 数据源加载钩子
     * @param generic 数据源json数据
     * @param properties 配置数据
     * @param applicationContext {@link ApplicationContext}
     */
    void dataSourceLoader(JSONObject generic, Properties properties, ApplicationContext applicationContext);
}
