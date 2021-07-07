package com.jw.screw.provider;


import com.jw.screw.loadbalance.BaseConfig;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import com.jw.screw.remote.netty.config.NettyServerConfig;

/**
 * provider的一些配置
 * @author jiangw
 * @date 2020/12/3 17:22
 * @since 1.0
 */
public class NettyProviderConfig extends BaseConfig {

    /**
     * rpc调用的连接数
     */
    private int connCount = 4;

    /**
     * 提供服务的权重
     */
    private int weight = 1;

    /**
     * 作为rpc调用的服务端
     */
    private NettyServerConfig rpcServerConfig;

    /**
     * 作为注册中心的客户端
     */
    private NettyClientConfig registerConfig;

    public int getConnCount() {
        return connCount;
    }

    public void setConnCount(int connCount) {
        this.connCount = connCount;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public NettyServerConfig getRpcServerConfig() {
        return rpcServerConfig;
    }

    public void setRpcServerConfig(NettyServerConfig rpcServerConfig) {
        this.rpcServerConfig = rpcServerConfig;
    }

    public NettyClientConfig getRegisterConfig() {
        return registerConfig;
    }

    public void setRegisterConfig(NettyClientConfig registerConfig) {
        this.registerConfig = registerConfig;
    }
}
