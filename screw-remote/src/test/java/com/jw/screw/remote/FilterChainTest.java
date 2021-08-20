package com.jw.screw.remote;

import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.transport.body.Body;
import com.jw.screw.remote.filter.AbstractFilter;
import com.jw.screw.remote.filter.FilterChain;
import com.jw.screw.remote.filter.FilterChainLoader;
import com.jw.screw.remote.filter.FilterContext;
import org.junit.Test;

public class FilterChainTest {

    @Test
    public void testChain() throws InterruptedException, RemoteTimeoutException, RemoteSendException {
        FilterChain filterChain = FilterChainLoader.loadChain(new MaxFilter(), new MinFilter());
        filterChain.process(null, null);
    }

    class MaxFilter extends AbstractFilter {

        @Override
        public <T extends FilterContext> void doFilter(Body body, T context, FilterChain next) throws InterruptedException, RemoteTimeoutException, RemoteSendException {
            System.out.println(MaxFilter.class.getName());
            if (next != null) {
                next.process(body, context);
            }
        }

        @Override
        public Integer weight() {
            return Integer.MAX_VALUE;
        }
    }

    class MinFilter extends AbstractFilter {

        @Override
        public <T extends FilterContext> void doFilter(Body body, T context, FilterChain next) throws InterruptedException, RemoteTimeoutException, RemoteSendException {
            System.out.println(MinFilter.class.getName());
            if (next != null) {
                next.process(body, context);
            }
        }

        @Override
        public Integer weight() {
            return Integer.MIN_VALUE;
        }
    }

}
