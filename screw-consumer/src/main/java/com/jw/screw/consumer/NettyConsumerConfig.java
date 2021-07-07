package com.jw.screw.consumer;

import com.jw.screw.loadbalance.BaseConfig;
import com.jw.screw.remote.netty.config.NettyClientConfig;

/**
 *
 * @author jiangw
 * @date 2020/12/4 11:10
 * @since 1.0
 */
public class NettyConsumerConfig extends BaseConfig {

    /**
     * rpc配置
     */
    private NettyClientConfig rpcConfig;

    /**
     * 注册中心配置
     */
    private NettyClientConfig registryConfig;

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
}
