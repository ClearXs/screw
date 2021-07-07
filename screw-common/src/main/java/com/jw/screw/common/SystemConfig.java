package com.jw.screw.common;

/**
 * screw 系统配置
 * @author jiangw
 * @date 2020/12/31 14:24
 * @since 1.0
 */
public enum SystemConfig {

    /**
     * 消费者
     */
    CONSUMER("consumer", "消费者", null),

    /**
     * 提供者
     */
    PROVIDER("provider", "提供者", null),

    /**
     * 注册中心
     */
    REGISTRY_CENTER("registry_center", "注册中心", 8501),

    /**
     * 配置中心
     */
    CONFIG_CENTER("config_center", "配置中心", 8601),

    /**
     * 监控中心
     */
    MONITOR_CENTER("monitor_center", "监控中心", 8701);

    /**
     * 名称
     */
    private final String role;

    /**
     * 描述
     */
    private final String description;

    /**
     * 默认端口
     */
    private final Integer defaultPort;

    SystemConfig(String role, String description, Integer defaultPort) {
        this.role = role;
        this.description = description;
        this.defaultPort = defaultPort;
    }

    public String getRole() {
        return role;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDefaultPort() {
        return defaultPort;
    }
}
