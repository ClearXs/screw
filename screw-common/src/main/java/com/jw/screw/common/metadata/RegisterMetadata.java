package com.jw.screw.common.metadata;

import com.jw.screw.common.transport.UnresolvedAddress;
import io.netty.channel.Channel;

import java.util.List;
import java.util.Objects;

/**
 * 注册的元数据
 * @author jiangw
 * @date 2020/11/29 17:08
 * @since 1.0
 */
public class RegisterMetadata {

    /**
     * 服务提供者的名称
     */
    private final String serviceProviderName;

    /**
     * 服务的权重，负载均衡使用
     */
    private int weight;

    /**
     * 建议连接数，与客户端连接数
     */
    private int connCount;

    /**
     * 发布的服务
     */
    private List<String> publishService;

    /**
     * 地址
     */
    private final UnresolvedAddress unresolvedAddress;

    /**
     * 所属的通道
     */
    private transient Channel channel;

    public RegisterMetadata(String providerServiceName, UnresolvedAddress socketAddress) {
        this(providerServiceName, 0, 4, socketAddress);
    }

    public RegisterMetadata(String serviceProviderName, int weight, int connCount, UnresolvedAddress unresolvedAddress) {
        this.serviceProviderName = serviceProviderName;
        this.weight = weight;
        this.connCount = connCount;
        this.unresolvedAddress = unresolvedAddress;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getConnCount() {
        return connCount;
    }

    public void setConnCount(int connCount) {
        this.connCount = connCount;
    }

    public List<String> getPublishService() {
        return publishService;
    }

    public void setPublishService(List<String> publishService) {
        this.publishService = publishService;
    }

    public UnresolvedAddress getUnresolvedAddress() {
        return unresolvedAddress;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RegisterMetadata that = (RegisterMetadata) o;
        return Objects.equals(serviceProviderName, that.serviceProviderName) &&
                Objects.equals(unresolvedAddress, that.unresolvedAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceProviderName, unresolvedAddress);
    }

    @Override
    public String toString() {
        return "RegisterMetadata{" +
                "providerServiceName='" + serviceProviderName + '\'' +
                ", weight=" + weight +
                ", connCount=" + connCount +
                ", publishService=" + publishService +
                '}';
    }
}
