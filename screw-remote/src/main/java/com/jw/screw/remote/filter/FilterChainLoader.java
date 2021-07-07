package com.jw.screw.remote.filter;

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * 一个过滤器链的加载器
 * @author jiangw
 * @date 2020/11/27 22:10
 * @since 1.0
 */
public class FilterChainLoader {

    /**
     * 根据{@link FilterChain}的过滤器进行权重排序
     * @param chain 过滤器链{@link FilterChain}
     * @return {@link FilterChain}
     */
    public synchronized static FilterChain loadPriority(FilterChain chain) {
        if (chain == null) {
            return null;
        }
        final FilterChain headChain = chain;
        // 轮询chain中所有的filter，并加入优先级队列中，随后进行重排序
        PriorityQueue<Filter> priorityFilter = new PriorityQueue<>();
        retry:
        for (;;) {
            Filter filter = chain.getFilter();
            priorityFilter.add(filter);
            if (chain.next() == null) {
                break retry;
            } else {
                chain = chain.next();
            }
        }
        // 重排序chain中的filter
        chain = headChain;
        retry:
        for (;;) {
            chain.setFilter(priorityFilter.poll());
            if (chain.next() == null) {
                break retry;
            } else {
                chain = chain.next();
            }
        }
        return headChain;
    }

    /**
     * 根据filter的权重加载filter chain
     * @param filters 过滤器{@link Filter}
     * @return {@link FilterChain}
     */
    public synchronized static FilterChain loadChain(Filter... filters) {
        if (filters.length == 0) {
            return null;
        }
        PriorityQueue<Filter> priorityFilter = new PriorityQueue<>(Arrays.asList(filters));
        Filter filter = priorityFilter.poll();
        final FilterChain headChain = new DefaultFilterChain(filter, null);
        FilterChain chain = headChain;
        while (!priorityFilter.isEmpty()) {
            DefaultFilterChain filterChain = new DefaultFilterChain(priorityFilter.poll(), null);
            chain.setNext(filterChain);
            chain = filterChain;
        }
        return headChain;
    }
}
