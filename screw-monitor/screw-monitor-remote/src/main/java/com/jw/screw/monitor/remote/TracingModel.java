package com.jw.screw.monitor.remote;

import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.monitor.opentracing.ScrewSpan;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * screw trace model
 * @author jiangw
 * @date 2020/12/25 10:02
 * @since 1.0
 */
public class TracingModel {

    /**
     * 服务key
     */
    private final String serverKey;

    /**
     * 服务address
     */
    private final UnresolvedAddress serverAddress;

    /**
     * key: trace id
     * value: span id -> root span
     */
    private final Map<String, ScrewSpan> tracers;

    public TracingModel(String serverKey, UnresolvedAddress serverAddress) {
        this.serverKey = serverKey;
        this.serverAddress = serverAddress;
        this.tracers = new ConcurrentHashMap<>();
    }

    public String getServerKey() {
        return serverKey;
    }

    public UnresolvedAddress getServerAddress() {
        return serverAddress;
    }

    public Map<String, ScrewSpan> getTracers() {
        return tracers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TracingModel that = (TracingModel) o;
        return Objects.equals(serverKey, that.serverKey) && Objects.equals(serverAddress, that.serverAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverKey, serverAddress);
    }
}
