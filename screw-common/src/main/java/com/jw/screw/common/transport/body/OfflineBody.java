package com.jw.screw.common.transport.body;


import com.jw.screw.common.metadata.RegisterMetadata;

/**
 * 下线body
 * @author jiangw
 * @date 2020/12/10 17:30
 * @since 1.0
 */
public class OfflineBody implements Body {

    private RegisterMetadata registerMetadata;

    public OfflineBody(RegisterMetadata registerMetadata) {
        this.registerMetadata = registerMetadata;
    }

    public RegisterMetadata getRegisterMetadata() {
        return registerMetadata;
    }

    public void setRegisterMetadata(RegisterMetadata registerMetadata) {
        this.registerMetadata = registerMetadata;
    }
}
