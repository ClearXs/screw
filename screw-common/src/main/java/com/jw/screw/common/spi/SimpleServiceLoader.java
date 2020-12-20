package com.jw.screw.common.spi;

import java.util.ServiceLoader;

/**
 * 一个简单的服务发现的加载器
 * 封装jdk提供一个简单调用方式
 * @author jiangw
 * @date 2020/11/27 9:24
 * @since 1.0
 */
public class SimpleServiceLoader {

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz).iterator().next();
    }
}
