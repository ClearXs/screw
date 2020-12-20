package com.jw.screw.consumer.filter;


import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.transport.body.RequestBody;

/**
 * 过滤器调用链，对请求进行过滤。基于责任链模式
 * 链中handler是FilterChain，由{@link Filter}包装
 * @author jiangw
 * @date 2020/11/27 21:50
 * @since 1.0
 */
public interface FilterChain {

    void setFilter(Filter filter);

    Filter getFilter();

    FilterChain next();

    void setNext(FilterChain filterChain);

    /**
     * 在链中对请求进行过滤
     */
    <T extends FilterContext> void process(RequestBody request, T context) throws InterruptedException, RemoteTimeoutException, RemoteSendException;
}
