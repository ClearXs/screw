package com.jw.screw.common.transport.body;

import com.jw.screw.common.metadata.ServiceMetadata;

/**
 * 订阅body
 * @author jiangw
 * @date 2020/12/10 17:31
 * @since 1.0
 */
public class SubscribeBody implements Body {

    private ServiceMetadata serviceMetadata;

    public SubscribeBody() {
    }

    public SubscribeBody(ServiceMetadata serviceMetadata) {
        this.serviceMetadata = serviceMetadata;
    }

    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    public void setServiceMetadata(ServiceMetadata serviceMetadata) {
        this.serviceMetadata = serviceMetadata;
    }
}
