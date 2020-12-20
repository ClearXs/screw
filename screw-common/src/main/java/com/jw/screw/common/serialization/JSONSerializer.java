package com.jw.screw.common.serialization;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.GsonBuilder;

/**
 * json 序列化
 * @author jiangw
 * @date 2020/12/10 17:31
 * @since 1.0
 */
public class JSONSerializer implements Serializer {

    private static final GsonBuilder BUILDER = new GsonBuilder();

    static {
        BUILDER.disableHtmlEscaping();
    }

    @Override
    public <T> byte[] serialization(T obj) {
        return BUILDER.create().toJson(obj).getBytes();
    }

    @Override
    public <T> T deserialization(byte[] bytes, Class<T> clazz) {
        return JSONObject.parseObject(new String(bytes), clazz);
    }
}
