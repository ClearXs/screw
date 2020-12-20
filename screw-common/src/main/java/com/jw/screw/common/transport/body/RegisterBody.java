package com.jw.screw.common.transport.body;


import com.jw.screw.common.metadata.RegisterMetadata;

import java.util.List;

/**
 * 服务注册body
 * @author jiangw
 * @date 2020/12/10 17:30
 * @since 1.0
 */
public class RegisterBody implements Body {

    private String serviceProviderName;

    private List<RegisterMetadata> registerMetadata;

    public RegisterBody(String serviceProviderName, List<RegisterMetadata> registerMetadata) {
        this.serviceProviderName = serviceProviderName;
        this.registerMetadata = registerMetadata;
    }

    public List<RegisterMetadata> getRegisterMetadata() {
        return registerMetadata;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }
}
