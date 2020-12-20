package com.jw.screw.consumer.filter;


import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.future.InvokeFuture;
import com.jw.screw.common.transport.body.RequestBody;
import com.jw.screw.consumer.invoker.Invoker;

/**
 * screw
 * @author jiangw
 * @date 2020/12/8 17:19
 * @since 1.0
 */
public class InvokerFilter extends AbstractFilter {

    @Override
    public <T extends FilterContext> void doFilter(RequestBody request, T context, FilterChain next) throws InterruptedException, RemoteTimeoutException, RemoteSendException {
        Invoker invoker = context.getInvoker();
        Object result = invoker.invoke(request, context);
        if (result instanceof InvokeFuture) {
            context.setFuture((InvokeFuture<?>) result);
            return;
        }
        context.setResult(result);
    }

    @Override
    public Integer weight() {
        return 1;
    }
}
