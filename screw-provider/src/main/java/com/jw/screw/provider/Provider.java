package com.jw.screw.provider;


import com.jw.screw.common.transport.UnresolvedAddress;

/**
 * 服务提供者的公共接口
 * @author jiangw
 * @date 2020/11/26 21:22
 * @since 1.0
 */
public interface Provider {

    /**
     * 启动对应的netty服务
     * 1.作为服务端，响应客户端的rpc调用
     * 2.作为客户端，响应注册中心的检测
     * 3.启动时，启动客户端与服务端。
     * 4.把自身的rpc服务注册到注册中修改
     */
    void start() throws InterruptedException;

    /**
     * 停止开启的服务
     */
    void shutdown() throws InterruptedException;

    /**
     * 注册服务提供者的相关的remote的处理器
     * 1.rpc调用的处理器
     * 2.注册中心的处理器
     */
    void registerProcessor();

    /**
     * 发布的相关服务
     * @param services
     */
    void publishServices(Object...services);

    /**
     * @param host
     * @param port
     */
    void registry(String host, int port);

    /**
     * 连接到注册中心
     */
    void registry(UnresolvedAddress registryAddress);


}
