package com.jw.screw.spring.properties;

import java.util.Properties;

/**
 * 提供者默认配置
 * @author jiangw
 * @date 2021/6/25 18:06
 * @since 1.0
 */
public class ProviderProperties implements DefaultProperties {

    // # 是否启用（可选，true or false）
    // enable=true
    // # 服务key（必须）
    // server.key=provider2
    // # 服务地址（必须，注意与tomcat等服务端口区分）
    // provider.server.port=8099
    // # 服务提供的地址（可选，若服务需要外网发布，那么需要填写外网转发的ip地址）
    // provider.address=localhost
    // # 注册中心地址（必须）
    // registry.address=localhost
    // # 注册中心端口（必须）
    // registry.port=8501
    // # 服务权重（可选）
    // weight=4
    // # 处理rpc请求的核心线程数（可选）
    // connCount=10
    // # 监控中心（可选）
    // monitor.key=monitor_center
    // # 监控指标收集周期（可选）
    // monitor.collect.period=10
    // # 需要发布服务包所在位置（可选）
    // provider.packageScan=

    @Override
    public Properties get() {
        Properties properties = new Properties();
        properties.put("enable", false);
        properties.put("server.key", "provider2");
        properties.put("provider.address", "localhost");
        properties.put("provider.server.port", 8099);
        properties.put("registry.address", "localhost");
        properties.put("registry.port", 8501);
        properties.put("weight", 4);
        properties.put("connCount", 10);
        properties.put("monitor.key", "monitor_center");
        properties.put("monitor.collect.period", 10);
        properties.put("provider.packageScan", "");
        return properties;
    }
}
