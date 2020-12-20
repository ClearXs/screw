package com.jw.screw.consumer;


import com.jw.screw.common.exception.ConnectionException;

/**
 * 观测连接
 * @author jiangw
 * @date 2020/12/1 15:30
 * @since 1.0
 */
public interface ConnectWatch {

    /**
     * 启动观测
     */
    void start() throws InterruptedException, ConnectionException;

    /**
     * 阻塞等待指定时间连接，超时或连接可用返回
     * @param millis
     * @return false 不可用
     * @throws InterruptedException
     */
    boolean waitForAvailable(long millis) throws InterruptedException;
}
