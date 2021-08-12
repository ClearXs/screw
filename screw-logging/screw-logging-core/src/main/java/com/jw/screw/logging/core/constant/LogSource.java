package com.jw.screw.logging.core.constant;

/**
 * 日志来源
 * @author jiangw
 * @date 2021/7/16 14:32
 * @since 1.1
 */
public interface LogSource {

    /**
     * 本地日志
     */
    String LOCAL_LOG = "local log";

    /**
     * rpc调用
     */
    String RPC = "rpc";

    /**
     * 外勤
     */
    String PATROL = "patrol";

    /**
     * hbp
     */
    String HBP = "hbp";

    /**
     * spring应用application name
     */
    String APPLICATION_NAME = "${APPLICATION_NAME}";
}
