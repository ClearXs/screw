package com.jw.screw.consumer;

import com.jw.screw.common.event.Observer;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.future.InvokeFuture;
import com.jw.screw.common.metadata.ServiceMetadata;

import java.util.concurrent.ExecutionException;

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
     * @param observer 观测者
     * @throws InterruptedException 线程中断异常
     * @throws ConnectionException 连接异常
     */
    void start(Observer observer) throws InterruptedException, ConnectionException, ExecutionException;

    /**
     * 关闭消费者
     * 1.关闭作为注册中心的客户端
     * 2.关闭作为rpc的客户端
     * 3.关闭Connector
     * @throws InterruptedException 线程中断异常
     */
    void stop() throws InterruptedException;

    /**
     * 调用远程的方法
     * @param serviceMetadata {@link ServiceMetadata}provider服务
     * @param serviceName 调用服务名，如DemoService
     * @param methodName 调用的方法名，如hello
     * @param returnType 方法返回的类型，{@link Class}
     * @param args 调用放的参数
     * @return RPC调用返回值
     */
    Object call(ServiceMetadata serviceMetadata, String serviceName, String methodName, Class<?> returnType, Object[] args);

    /**
     * 异步调用远程方法
     * @param serviceMetadata {@link ServiceMetadata}provider服务
     * @param serviceName 调用服务名，如DemoService
     * @param methodName 调用的方法名，如hello
     * @param returnType 方法返回的类型，{@link Class}
     * @param args 调用放的参数
     * @return {@link InvokeFuture}
     */
    InvokeFuture<?> asyncCall(ServiceMetadata serviceMetadata, String serviceName, String methodName, Class<?> returnType, Object[] args);

    /**
     * 根据服务元数据获取连接观测者
     * @param serviceMetadata {@link ServiceMetadata}
     * @throws InterruptedException 线程中断异常
     * @throws ConnectionException 连接异常
     * @return {@link com.jw.screw.consumer.ConnectionWatcher}
     */
    com.jw.screw.consumer.ConnectionWatcher watchConnect(ServiceMetadata serviceMetadata) throws InterruptedException, ConnectionException;
}
