package com.jw.screw.remote.filter;

import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.transport.body.Body;
import com.jw.screw.remote.RemoteService;

/**
 * 作为处理过滤响应的最后一环
 * @author jiangw
 * @date 2020/12/24 11:38
 * @since 1.0
 */
public class ProcessResponseFilter extends AbstractFilter {

    @Override
    public <T extends FilterContext> void doFilter(Body body, T context, FilterChain next) throws InterruptedException, RemoteTimeoutException, RemoteSendException {
        // 作为client端，处理server的响应，比如说远程调用请求的响应
        RemoteService remoteService = context.getRemoteService();
        if (remoteService != null) {
            remoteService.processRemoteResponse(context.getCtx(), context.getTransporter());
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
