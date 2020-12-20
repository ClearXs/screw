package com.jw.screw.common.transport;

import java.util.Objects;

/**
 * 远程地址model
 * @author jiangw
 * @date 2020/12/10 17:31
 * @since 1.0
 */
public class RemoteAddress implements UnresolvedAddress {

    private final int port;

    private final String host;

    public RemoteAddress(String host, int port) {
        this.port = port;
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RemoteAddress that = (RemoteAddress) o;
        return port == that.port &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, host);
    }

    @Override
    public String toString() {
        return "RemoteAddress{" +
                "port=" + port +
                ", address='" + host + '\'' +
                '}';
    }
}
