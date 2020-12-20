package com.jw.screw.consumer.filter;


import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.transport.body.RequestBody;

/**
 * screw
 * @author jiangw
 * @date 2020/12/8 17:19
 * @since 1.0
 */
public class RequestFilter extends AbstractFilter {

    private final String methodName;

    private final Object[] parameters;

    public RequestFilter(String methodName, Object[] parameters) {
        this.methodName = methodName;
        this.parameters = parameters;
    }

    @Override
    public <T extends FilterContext> void doFilter(RequestBody request, T context, FilterChain next) throws InterruptedException, RemoteTimeoutException, RemoteSendException {
        request.setMethodName(methodName);
        request.setParameters(parameters);
        if (next != null) {
            next.process(request, context);
        }
    }
}
