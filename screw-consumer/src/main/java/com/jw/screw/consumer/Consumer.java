package com.jw.screw.consumer;


import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.future.InvokeFuture;
import com.jw.screw.common.metadata.ServiceMetadata;

/**
 * screw
 * @author jiangw
 * @date 2020/12/8 17:18
 * @since 1.0
 */
public interface Consumer {

    /**
     * 启动消费者Netty服务
     * 1.作为注册中心客户端
     * 2.作为远程服务的客户端
     */
    void start() throws InterruptedException;

    /**
     * 关闭消费者
     * 1.关闭作为注册中心的客户端
     * 2.关闭作为rpc的客户端
     * 3.关闭Connector
     */
    void stop() throws InterruptedException;

    /**
     * 调用远程的方法
     */
    Object call(ServiceMetadata serviceMetadata, String serviceName, String methodName, Class<?> returnType, Object[] args);

    /**
     * 异步调用远程方法
     * @return
     */
    InvokeFuture<?> asyncCall(ServiceMetadata serviceMetadata, String serviceName, String methodName, Class<?> returnType, Object[] args);

    /**
     * 观测远程服务
     * @param serviceMetadata
     */
    ConnectWatch watchConnect(ServiceMetadata serviceMetadata) throws InterruptedException, ConnectionException;
}
