package com.jw.screw.provider;


import com.jw.screw.remote.netty.config.NettyClientConfig;
import com.jw.screw.remote.netty.config.NettyServerConfig;

/**
 * provider的一些配置
 * @author jiangw
 * @date 2020/12/3 17:22
 * @since 1.0
 */
public class NettyProviderConfig {

    /**
     * 提供的名称
     */
    private String providerKey;

    /**
     * rpc调用的连接数
     */
    private int connCount = 4;

    /**
     * 提供服务的权重
     */
    private int weight = 1;

    /**
     * 提供的端口
     */
    private int port = 8080;

    /**
     * 作为rpc调用的服务端
     */
    private NettyServerConfig rpcServerConfig;

    /**
     * 作为注册中心的客户端
     */
    private NettyClientConfig registerConfig;


    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
