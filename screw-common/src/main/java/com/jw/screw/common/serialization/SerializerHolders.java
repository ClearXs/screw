package com.jw.screw.common.serialization;


import com.jw.screw.common.spi.SimpleServiceLoader;

/**
 * 简单的spi注入
 * @author jiangw
 * @date 2020/12/10 17:32
 * @since 1.0
 */
public class SerializerHolders {

    private static Serializer serializer = SimpleServiceLoader.load(Serializer.class);

    public static Serializer serializer() {
        return serializer;
    }
}
