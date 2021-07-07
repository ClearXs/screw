package com.jw.screw.consumer;

import com.jw.screw.common.exception.ConnectionException;

/**
 * 连接观测者
 * @author jiangw
 * @date 2020/12/1 15:30
 * @since 1.0
 */
public interface ConnectionWatcher {

    /**
     * 启动观测
     * @throws InterruptedException 线程中断时异常
     * @throws ConnectionException 连接异常
     */
    void start() throws InterruptedException, ConnectionException;

    /**
     * 阻塞等待指定时间连接，超时或连接可用返回
     * @param millis 等待的ms
     * @return false 不可用
     * @throws InterruptedException 线程中断异常
     */
    boolean waitForAvailable(long millis) throws InterruptedException;
}
