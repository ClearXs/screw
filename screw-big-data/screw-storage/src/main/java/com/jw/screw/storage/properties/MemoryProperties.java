package com.jw.screw.storage.properties;

import lombok.Data;

/**
 * 内存
 * @see java.util.concurrent.ConcurrentHashMap#ConcurrentHashMap(int, float, int)
 * @author jiangw
 * @date 2021/7/21 17:03
 * @since 1.1
 */
@Data
public class MemoryProperties {

    /**
     * 容量
     */
    private int capacity = 16;

    /**
     * 负载因子 0 ~ 1之间
     */
    private float loadFactor = 0.75f;

    /**
     * 并发级别
     * 确认segment的数量，它的值是不小于concurrencyLevel的第一个2^n的数，假如concurrencyLevel数量的线程访问
     * Map，那么这些线程恰好就落到每个segment中
     */
    private int concurrencyLevel = 1;
}
