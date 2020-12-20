package com.jw.screw.consumer.invoker;


import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.transport.body.RequestBody;
import com.jw.screw.consumer.filter.FilterContext;

/**
 * 远程调用的具体实现
 * @author jiangw
 * @date 2020/11/27 21:47
 * @since 1.0
 */
public interface Invoker {

    /**
     * 传递方法名与参数可以调用服务
     * @return
     */
    <T extends FilterContext> Object invoke(RequestBody request, T context) throws InterruptedException, RemoteTimeoutException, RemoteSendException;
}
