package com.jw.screw.monitor.core;

import com.jw.screw.common.SystemConfig;
import com.jw.screw.common.transport.UnresolvedAddress;

import java.util.Objects;

/**
 * 监控服务model
 * @author jiangw
 * @date 2020/12/22 17:33
 * @since 1.0
 */
public class MonitorModel {

    private final String serverKey;

    private final UnresolvedAddress address;

    /**
     * 角色{@link SystemConfig}
     */
    private String role;

    private long lastUpdateTime;

    public MonitorModel(String serverKey, UnresolvedAddress address) {
        this.serverKey = serverKey;
        this.address = address;
        lastUpdateTime = System.currentTimeMillis();
    }

    public String getServerKey() {
        return serverKey;
    }

    public UnresolvedAddress getAddress() {
        return address;
    }

    public void updateTime() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * 判断当前服务是否已经死亡
     * @param delayedTime 判断死亡的时长
     * @return true 死亡， false 否
     */
    public boolean judgeDies(long delayedTime) {
        return delayedTime + lastUpdateTime < System.currentTimeMillis();
    }

    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MonitorModel that = (MonitorModel) o;
        return Objects.equals(serverKey, that.serverKey) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverKey, address);
    }
}
