package com.jw.screw.registry;

/**
 * 注册中心
 * @author jiangw
 * @date 2020/11/29 16:56
 * @since 1.0
 */
public interface Registry {

    /**
     * 启动注册中心
     */
    void start();

    /**
     * 关闭注册中心
     */
    void shutdown();

}
