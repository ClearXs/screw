package com.jw.screw.remote;

import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.transport.body.Body;

/**
 * 调用
 * @author jiangw
 * @date 2020/11/27 21:47
 * @since 1.0
 */
public interface Invoker {

    /**
     * 传递方法名与参数可以调用服务
     * @param body 传输的{@link Body}
     * @param remoteService 调用service
     * @return 调用结果
     * @throws InterruptedException
     * @throws RemoteSendException
     * @throws RemoteTimeoutException
     */
    Object invoke(Body body, RemoteService remoteService) throws InterruptedException, RemoteTimeoutException, RemoteSendException;
}
