package com.jw.screw.spring;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.jw.screw.common.constant.StringPool;

import java.util.Map;
import java.util.Properties;

/**
 * 根据配置中心提供的模板对配置进行解析 基于fastjson
 * 因为配置是以json形式存在，所以会存在多个子树。按如下约定：
 * 约定1：
 *  如果配置是：{config: {subConfig: value}}形式
 *  解析出来的配置结果是：config.subConfig
 * 约定2：
 *  如果配置是：{config: [{subConfig: value}]}
 *  解析出来的配置结果是：config[0].subConfig
 * 约定3：
 *  作为array的数据一定是key-value形式
 * @author jiangw
 * @date 2020/12/10 21:58
 * @since 1.0
 */
public class ConfigParser {

    public static Properties getProperties(String config) {
        Properties properties = new Properties();
        try {
            JSONObject configObj = JSONObject.parseObject(config);
            parseObject(configObj, properties, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private static StringBuilder parseObject(JSONObject obj, Properties properties, StringBuilder builder) {
        for (Map.Entry<String, Object> entry : obj.entrySet()) {
            if (builder == null) {
                builder = new StringBuilder();
            }
            builder.append(StringPool.DOT).append(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof JSONObject) {
                // 1.判断当前配置是否存在object
                builder = parseObject((JSONObject) value, properties, builder);
                builder = new StringBuilder(builder.toString().replace(StringPool.DOT + entry.getKey(), ""));
            } else if (value instanceof JSONArray) {
                // 2.判断当前配置是否存在array
                parseArray((JSONArray) value, properties, builder);
            } else {
                // 去除第一个.
                properties.setProperty(builder.substring(1), value.toString());
                // build去除当前添加的entry.key
                builder = new StringBuilder(builder.toString().replace(StringPool.DOT + entry.getKey(), ""));
            }
        }
        return builder;
    }

    private static void parseArray(JSONArray array, Properties properties, StringBuilder builder) {
        String origin = builder.toString();
        for (int i = 0; i < array.size(); i++) {
            Object obj = array.get(i);
            if (obj instanceof JSONObject) {
                builder = new StringBuilder(origin);
                builder.append(StringPool.LEFT_SMALL_BRACKETS).append(i).append(StringPool.RIGHT_SMALL_BRACKETS);
                builder = parseObject((JSONObject) obj, properties, builder);
            } else if (obj instanceof JSONArray) {
                parseArray((JSONArray) obj, properties, builder);
            }
        }
    }
}
