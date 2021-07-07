package com.jw.screw.monitor.core;

/**
 * 服务健康状态
 * @author jiangw
 * @date 2020/12/22 17:24
 * @since 1.0
 */
public interface HealthStatus {

    /**
     * health green
     */
    String HEALTH = "health";

    /**
     * close gray
     */
    String CLOSE = "close";

    /**
     * waring orange
     */
    String WARNING = "waring";
}
