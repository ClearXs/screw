package com.jw.screw.monitor.core.body;

import com.jw.screw.common.SystemConfig;
import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.transport.body.Body;
import com.jw.screw.monitor.core.mircometer.Metrics;

import java.util.List;
import java.util.Map;

/**
 * 远程的消息体
 * @author jiangw
 * @date 2020/12/22 14:45
 * @since 1.0
 */
public class MetricsBody implements Body {

    /**
     * 服务key，这个服务可能是消费者或提供者、注册中心等
     */
    private String serverKey;

    /**
     * 服务的地址
     */
    private UnresolvedAddress address;

    /**
     * 性能-map
     * key: 性能名
     * value: {@link Metrics}
     */
    private Map<String, List<Metrics>> metricsMap;

    /**
     * 角色{@link SystemConfig}
     */
    private String role;

    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    public UnresolvedAddress getAddress() {
        return address;
    }

    public void setAddress(UnresolvedAddress address) {
        this.address = address;
    }

    public Map<String, List<Metrics>> getMetricsMap() {
        return metricsMap;
    }

    public void setMetricsMap(Map<String, List<Metrics>> metricsMap) {
        this.metricsMap = metricsMap;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "MetricsBody{" +
                "serverKey='" + serverKey + '\'' +
                ", address=" + address +
                ", metricsMap=" + metricsMap +
                ", role='" + role + '\'' +
                '}';
    }
}
