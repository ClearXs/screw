package com.jw.screw.spring.properties;

import java.util.Properties;

/**
 * 监控中心默认配置
 * @author jiangw
 * @date 2021/6/25 18:05
 * @since 1.0
 */
public class MonitorProperties implements DefaultProperties {

    // # 监控中心key
    // server.key=monitor_center
    // # 服务提供的端口
    // server.port=8701
    // # 注册中心地址
    // registry.address=localhost
    // # 注册中心端口
    // registry.port=8501
    // # 权重
    // weight=4
    // # 处理rpc请求的核心线程数（可选）
    // connCount=10

    @Override
    public Properties get() {
        Properties properties = new Properties();
        properties.put("server.key", "monitor_center");
        properties.put("server.port", 8701);
        properties.put("registry.address", "localhost");
        properties.put("registry.port", 8501);
        properties.put("weight", 4);
        properties.put("connCount", 10);
        return null;
    }
}
