package com.jw.screw.loadbalance;


import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.common.util.Collections;
import com.jw.screw.remote.netty.SConnector;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jiangw
 * @date 2020/12/3 16:39
 * @since 1.0
 */
public abstract class AbstractRule implements Rule {

    protected final AtomicInteger retryCount = new AtomicInteger(0);

    /**
     * 轮询重试次数
     */
    protected final static int RETRY_NUMS = 10;

    private LoadBalancer loadBalancer;

    @Override
    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    protected LoadBalancer getLoadBalancer() {
        return this.loadBalancer;
    }

    protected int randomInt(int serviceCount) {
        return ThreadLocalRandom.current().nextInt(serviceCount);
    }

    protected List<SConnector> getConnectors() {
        LoadBalancer loadBalancer = getLoadBalancer();
        Map<UnresolvedAddress, SConnector> serviceInfos = loadBalancer.getServersList();
        if (Collections.isEmpty(serviceInfos)) {
            return null;
        }
        List<SConnector> connectors = Collections.toList(serviceInfos);
        if (Collections.isEmpty(connectors)) {
            return null;
        }
        return connectors;
    }
}
