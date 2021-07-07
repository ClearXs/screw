package com.jw.screw.common.metadata;

import lombok.Data;

import java.util.Objects;

/**
 * 服务器元数据
 * @author jiangw
 * @date 2020/11/28 13:58
 * @since 1.0
 */
@Data
public class ServiceMetadata {

    private String serviceProviderName;

    public ServiceMetadata(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }
}
