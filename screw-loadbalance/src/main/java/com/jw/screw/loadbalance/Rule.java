package com.jw.screw.loadbalance;

import com.jw.screw.remote.netty.SConnector;

/**
 * screw
 * @author jiangw
 * @date 2020/12/9 9:23
 * @since 1.0
 */
public interface Rule {

    /**
     * 根据不同的负载均衡算法获取可用的服务连接器
     * @return
     */
    SConnector chose();

    /**
     * 设置负载均衡器
     */
    void setLoadBalancer(LoadBalancer loadBalancer);
}
