package com.jw.screw.loadbalance;

import com.jw.screw.remote.netty.SConnector;

/**
 * screw
 * @author jiangw
 * @date 2020/12/9 9:22
 * @since 1.0
 */
public class RuleContext {

    private final Rule rule;

    public RuleContext(LoadBalancer loadBalancer) {
        this(loadBalancer, RuleObjectFactory.WEIGHT_RANDOM.getRule());
    }

    public RuleContext(LoadBalancer loadBalancer, Rule rule) {
        if (rule == null) {
            rule = RuleObjectFactory.WEIGHT_RANDOM.getRule();
        }
        this.rule = rule;
        rule.setLoadBalancer(loadBalancer);
    }

    public SConnector select() {
        return rule.chose();
    }
}
