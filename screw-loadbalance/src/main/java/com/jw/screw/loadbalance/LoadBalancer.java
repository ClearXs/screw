package com.jw.screw.loadbalance;

import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.remote.netty.SConnector;

import java.util.Map;

/**
 * screw
 * @author jiangw
 * @date 2020/12/9 9:23
 * @since 1.0
 */
public interface LoadBalancer {

    /**
     * 按照负载均衡策略获取一个可用的服务
     * @return
     */
    SConnector selectServer();

    Map<UnresolvedAddress, SConnector> getServersList();

    void setRule(Rule rule);

}
