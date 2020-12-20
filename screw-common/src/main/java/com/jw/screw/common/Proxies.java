package com.jw.screw.common;

import com.jw.screw.common.util.Requires;

/**
 * screw
 * @author jiangw
 * @date 2020/12/9 15:14
 * @since 1.0
 */
public class Proxies {

    private final static String ASYNC = "ASYNC";

    private final static String SYNC = "SYNC";

    /**
     * 解析调用的类型
     * @param invokeType ASYNC or SYNC
     * @return true 异步. false 同步
     */
    public static boolean parseInvokeType(String invokeType) {
        Requires.isNull(invokeType, "invokeType");
        if (ASYNC.equals(invokeType)) {
            return true;
        } else if (SYNC.equals(invokeType)) {
            return false;
        }
        throw new IllegalArgumentException("error invoke type. either ASYNC or SYNC.");
    }
}
