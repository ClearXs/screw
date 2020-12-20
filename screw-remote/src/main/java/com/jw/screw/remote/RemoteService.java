package com.jw.screw.remote;

import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;

/**
 * @author jiangw
 * @date 2020/12/7 21:02
 * @since 1.0
 */
public interface RemoteService {

    /**
     * 初始化一些配置
     */
    void init();

    /**
     * 启动服务.客户端或服务器端
     */
    void start();

    /**
     * 并不使用强制关闭
     */
    void shutdownGracefully() throws InterruptedException;

    /**
     * 对请求时异步调用，对返回的结果时同步阻塞。
     * @param channel 调用的通道
     * @param request 请求
     * @param timeout 超时时间
     */
    RemoteTransporter syncInvoke(Channel channel, RemoteTransporter request, long timeout) throws InterruptedException, RemoteTimeoutException, RemoteSendException;

    /**
     * 实现未阻塞的异步调用
     * @param channel
     * @param request
     * @return
     */
    Object asyncInvoke(Channel channel, RemoteTransporter request, Class<?> returnType);

    /**
     * 对于服务提供者，需要注册远程调用的处理器
     * 对于消费者，需要注册订阅结果的处理器。。。
     * @param code 消息的类型{@link Protocol.Code}
     * @param processor 对于的处理器
     * @param exec 处理业务逻辑的线程池
     */
    void registerProcessors(byte code, NettyProcessor processor, ExecutorService exec);
}
