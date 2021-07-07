package com.jw.screw.monitor.api;

/**
 * monitor
 * @author jiangw
 * @date 2020/12/23 10:32
 * @since 1.0
 */
public interface Monitor {

    /**
     * 发送性能指标、链路信息
     */
    void send();
}
