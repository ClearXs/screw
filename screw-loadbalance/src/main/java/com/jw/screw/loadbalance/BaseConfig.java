package com.jw.screw.loadbalance;

import com.jw.screw.common.SConfig;
import com.jw.screw.remote.Remotes;

import java.util.concurrent.TimeUnit;

/**
 * abstract config
 * @author jiangw
 * @date 2020/12/23 11:46
 * @since 1.0
 */
public class BaseConfig implements SConfig {

    /**
     * 本身服务的key（无论是作为消费者还是提供者）
     */
    private String serverKey;

    /**
     * 本身服务的ip地址
     */
    private String serverHost = Remotes.getHost();

    /**
     * 提供的端口
     */
    private int port = 8080;

    /**
     * 配置中心key
     */
    private String configServerKey;

    /**
     * 监控中心key
     */
    private String monitorServerKey;

    /**
     * 监控收集周期
     */
    private int monitorCollectPeriod = 10;

    /**
     * 监控收集单位
     */
    private TimeUnit monitorCollectUnit = TimeUnit.SECONDS;

    /**
     * rpc负载均衡算法
     */
    private Rule rule;

    /**
     * 角色
     */
    private String role;

    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getConfigServerKey() {
        return configServerKey;
    }

    public void setConfigServerKey(String configServerKey) {
        this.configServerKey = configServerKey;
    }

    public String getMonitorServerKey() {
        return monitorServerKey;
    }

    public void setMonitorServerKey(String monitorServerKey) {
        this.monitorServerKey = monitorServerKey;
    }

    public int getMonitorCollectPeriod() {
        return monitorCollectPeriod;
    }

    public void setMonitorCollectPeriod(int monitorCollectPeriod) {
        this.monitorCollectPeriod = monitorCollectPeriod;
    }

    public TimeUnit getMonitorCollectUnit() {
        return monitorCollectUnit;
    }

    public void setMonitorCollectUnit(TimeUnit monitorCollectUnit) {
        this.monitorCollectUnit = monitorCollectUnit;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
