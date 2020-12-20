package com.jw.screw.common.util;

import com.jw.screw.common.SnowflakeIdWorker;

/**
 * id工具类
 * @author jiangw
 * @date 2020/12/8 22:31
 * @since 1.0
 */
public class IdUtils {

    private static final SnowflakeIdWorker ID_WORKER;

    static {
        ID_WORKER = new SnowflakeIdWorker(0, 0);
    }

    public static long getNextId() {
        return ID_WORKER.nextId();
    }

}
