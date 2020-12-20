package com.jw.screw.common.config;

import java.util.concurrent.TimeUnit;

/**
 * 注册中心的相关配置
 * @author jiangw
 * @date 2020/11/27 9:54
 * @since 1.0
 */
public interface RegisterConfig {

    /**
     * 定时发送时间
     */
    int delayPublish = 60;

    /**
     * 定时发送周期
     */
    int delayPeriod = 60;

    /**
     * 定时发送的单位
     */
    TimeUnit delayUnit = TimeUnit.SECONDS;
}
