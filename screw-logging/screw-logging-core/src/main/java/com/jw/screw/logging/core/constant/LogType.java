package com.jw.screw.logging.core.constant;

public interface LogType {

    /**
     * 采用slf4j进行的输出的日志，进行记录
     */
    String SLF4J_LOG = "slf4j log";

    /**
     * 服务本地的功能日志，可能是用户服务
     */
    String FUNCTION_LOG = "function log";

    /**
     * 远程调用日志
     */
    String RPC_LOG = "rpc log";
}
