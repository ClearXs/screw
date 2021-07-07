package com.jw.screw.spring.properties;

import java.util.Properties;

/**
 * 消费者端的配置
 * @author jiangw
 * @date 2021/6/25 17:27
 * @since 1.0
 */
public class ConsumerProperties implements DefaultProperties {

    // # 是否启用（可选，true or false）
    // enable=true
    // # 服务key（必须，消费者的server）
    // server.key=ba1e031e5d722a763536b026138f678c
    // # 服务port（可选，如果当前服务有作为provider需要填上provider port）
    // server.port=8085
    // # 注册中心地址（必须）
    // registry.address=localhost
    // # 注册中心端口（必须）
    // registry.port=8501
    // # 连接等待时长 unit mills（可选）
    // waitMills=30000
    // # 负载均衡策略（可选）
    // loadbalance=RANDOM_WEIGHT
    // # 配置中心key（可选）
    // config.key=config_center
    // # 监控中心（可选）
    // monitor.key=monitor_center
    // # 监控指标收集周期 unit s（可选）
    // monitor.collect.period=10
    // # 当前服务作为provider时，需要填与服务提供者提供的端口，用于监控中心（服务作为provider必填）
    // provider.server.port=8080

    @Override
    public Properties get() {
        Properties properties = new Properties();
        properties.put("enable", false);
        properties.put("server.key", "");
        properties.put("server.port", "");
        properties.put("registry.address", "localhost");
        properties.put("registry.port", 8501);
        properties.put("waitMills", 30000);
        properties.put("loadbalance", "RANDOM_WEIGHT");
        properties.put("config.key", "config_center");
        properties.put("monitor.key", "monitor_center");
        properties.put("monitor.collect.period", 10);
        properties.put("provider.server.port", 8080);
        return properties;
    }
}
