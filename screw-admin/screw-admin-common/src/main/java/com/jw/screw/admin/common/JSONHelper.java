package com.jw.screw.admin.common;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * json数据的校验，拼装
 * @author jiangw
 * @date 2020/11/17 16:14
 * @since 1.0
 */
public class JSONHelper {

    public static JSONObject validate(String jsonData) {
        try {
            return JSONObject.parseObject(jsonData);
        } catch (Exception e) {
            return null;
        }
    }

    public static String assemble(Map<String, Object> keyValues) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                jsonObject.put(key, "");
                continue;
            }
            JSONObject object = validate(value.toString());
            if (object != null) {
                jsonObject.put(key, object);
            } else {
                jsonObject.put(key, value);
            }
        }
        return jsonObject.toJSONString();
    }
}
