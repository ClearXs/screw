package com.jw.screw.monitor.opentracing;

import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.transport.body.Body;

/**
 * trace
 * @author jiangw
 * @date 2020/12/24 21:55
 * @since 1.0
 */
public class TraceBody implements Body {

    private UnresolvedAddress serverAddress;

    private String serverKey;

    private ScrewSpan screwSpan;

    public UnresolvedAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(UnresolvedAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    public ScrewSpan getScrewSpan() {
        return screwSpan;
    }

    public void setScrewSpan(ScrewSpan screwSpan) {

        this.screwSpan = screwSpan;
    }
}
