package com.jw.screw.common.serialization;

/**
 * screw
 * @author jiangw
 * @date 2020/12/10 17:32
 * @since 1.0
 */
public interface Serializer {

    /**
     * 对象序列化
     * @author jiangw
     * @date 2020/11/22 23:57
     * @since 1.0
     */
    <T> byte[] serialization(T obj);

    /**
     * 对象反序列化
     */
    <T> T deserialization(byte[] bytes, Class<T> clazz);
}
