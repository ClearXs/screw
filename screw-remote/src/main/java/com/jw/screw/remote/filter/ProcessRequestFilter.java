package com.jw.screw.remote.filter;

import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.transport.body.Body;
import com.jw.screw.remote.RemoteService;

/**
 * 作为处理过滤请求的最后一环
 * @author jiangw
 * @date 2020/12/24 11:46
 * @since 1.0
 */
public class ProcessRequestFilter extends AbstractFilter {

    @Override
    public <T extends FilterContext> void doFilter(Body body, T context, FilterChain next) throws InterruptedException, RemoteTimeoutException, RemoteSendException {
        // 作为server端，处理client的请求，比如说，注册请求，订阅请求，远程调用请求
        RemoteService remoteService = context.getRemoteService();
        if (remoteService != null) {
            remoteService.processRemoteRequest(context.getCtx(), context.getTransporter());
        }
        if (next != null) {
            next.process(body, context);
        }
    }

    @Override
    public Integer weight() {
        return Integer.MAX_VALUE;
    }
}
