package com.jw.screw.common.util;

import com.jw.screw.common.SnowflakeIdWorker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * id工具类
 * @author jiangw
 * @date 2020/12/8 22:31
 * @since 1.0
 */
public class IdUtils {

    private static final SnowflakeIdWorker[] ID_WORKERS;

    private static final int MAX_DATA_CENTER_ID = 31;

    private static final AtomicInteger NEXT = new AtomicInteger(0);

    static {
        ID_WORKERS = new SnowflakeIdWorker[MAX_DATA_CENTER_ID];
        for (int i = 0; i < MAX_DATA_CENTER_ID; i++) {
            ID_WORKERS[i] = new SnowflakeIdWorker(0, i);
        }
    }

    public static long getNextId() {
        int next = NEXT.getAndIncrement();
        if (next == MAX_DATA_CENTER_ID) {
            next = 0;
            NEXT.set(next);
        }
        return ID_WORKERS[next].nextId();
    }

    public static String getNextIdAsString() {
        return String.valueOf(getNextId());
    }
}
