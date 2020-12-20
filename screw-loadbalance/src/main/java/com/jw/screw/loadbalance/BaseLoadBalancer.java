package com.jw.screw.loadbalance;


import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.remote.netty.SConnector;

import java.util.Map;

/**
 * @author jiangw
 * @date 2020/12/3 16:42
 * @since 1.0
 */
public class BaseLoadBalancer implements LoadBalancer {

    private final Map<UnresolvedAddress, SConnector> serviceInfo;

    private Rule rule;

    private RuleContext ruleContext;

    public BaseLoadBalancer(Map<UnresolvedAddress, SConnector> serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Override
    public SConnector selectServer() {
        if (ruleContext == null) {
            ruleContext = new RuleContext(this, rule);
        }
        return ruleContext.select();
    }

    @Override
    public Map<UnresolvedAddress, SConnector> getServersList() {
        return serviceInfo;
    }

    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
    }
}
