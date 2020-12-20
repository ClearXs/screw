package com.jw.screw.consumer.filter;


import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.transport.body.RequestBody;

/**
 * screw
 * @author jiangw
 * @date 2020/12/8 17:18
 * @since 1.0
 */
public class DefaultFilterChain implements FilterChain {

    private Filter filter;

    private FilterChain next;

    public DefaultFilterChain(Filter filter, FilterChain next) {
        this.filter = filter;
        this.next = next;
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public FilterChain next() {
        return next;
    }

    @Override
    public void setNext(FilterChain next) {
        this.next = next;
    }

    @Override
    public <T extends FilterContext> void process(RequestBody request, T context) throws InterruptedException, RemoteTimeoutException, RemoteSendException {
        filter.doFilter(request, context, next);
    }
}
