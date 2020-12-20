package com.jw.screw.common.transport.body;


import com.jw.screw.common.transport.UnresolvedAddress;

import java.util.Arrays;
import java.util.List;

/**
 * 发布的服务的信息体
 * @author jiangw
 * @date 2020/11/29 17:25
 * @since 1.0
 */
public class PublishBody implements Body {

    /**
     * 地址信息
     */
    private UnresolvedAddress publishAddress;

    /**
     * 发布的服务名
     */
    private String serviceName;

    /**
     * 发布的服务名
     * value=包名
     */
    private List<String> publishServices;

    /**
     * 服务建议的连接数
     */
    private int connCount;

    /**
     * 服务的权重
     */
    private int wight;

    public UnresolvedAddress getPublishAddress() {
        return publishAddress;
    }

    public void setPublishAddress(UnresolvedAddress publishAddress) {
        this.publishAddress = publishAddress;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getPublishServices() {
        return publishServices;
    }

    public void setPublishServices(String... publishServices) {
        this.publishServices = Arrays.asList(publishServices);
    }

    public int getConnCount() {
        return connCount;
    }

    public void setConnCount(int connCount) {
        this.connCount = connCount;
    }

    public int getWight() {
        return wight;
    }

    public void setWight(int wight) {
        this.wight = wight;
    }
}
