package com.jw.screw.common.metadata;

import java.util.Objects;

/**
 * 服务器元数据
 * @author jiangw
 * @date 2020/11/28 13:58
 * @since 1.0
 */
public class ServiceMetadata {

    private String serviceProviderName;

    public ServiceMetadata(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceMetadata that = (ServiceMetadata) o;
        return Objects.equals(serviceProviderName, that.serviceProviderName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceProviderName);
    }

    @Override
    public String toString() {
        return "ServiceMetadata{" +
                "serviceProviderName='" + serviceProviderName + '\'' +
                '}';
    }
}
