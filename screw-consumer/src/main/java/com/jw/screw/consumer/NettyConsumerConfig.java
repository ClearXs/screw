package com.jw.screw.consumer;


import com.jw.screw.loadbalance.Rule;
import com.jw.screw.remote.netty.config.NettyClientConfig;

/**
 *
 * @author jiangw
 * @date 2020/12/4 11:10
 * @since 1.0
 */
public class NettyConsumerConfig {

    /**
     * rpc配置
     */
    private NettyClientConfig rpcConfig;

    /**
     * 注册中心配置
     */
    private NettyClientConfig registryConfig;

    /**
     * rpc负载均衡算法
     */
    private Rule rule;

    public NettyClientConfig getRpcConfig() {
        return rpcConfig;
    }

    public void setRpcConfig(NettyClientConfig rpcConfig) {
        this.rpcConfig = rpcConfig;
    }

    public NettyClientConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistryConfig(NettyClientConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }
}
