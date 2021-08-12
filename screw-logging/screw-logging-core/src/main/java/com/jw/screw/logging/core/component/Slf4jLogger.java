package com.jw.screw.logging.core.component;

import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.Executor;

/**
 * <b>提供slf4j本地日志存储到远程或者某个数据数据收集器中</b>
 * <p>参考自：<a href="https://gitee.com/plumeorg/plumelog?_from=gitee_search">Plumelog</a></p>
 * @author jiangw
 * @date 2021/7/7 16:02
 * @since 1.1
 */
public interface Slf4jLogger {

    /**
     *
     * @param message 消息模型数据（暂定） {@link Message}
     * @param executor 记录执行器
     * @throws Exception
     */
    default void send(Message message, Executor executor) throws Exception {
        if (executor != null) {
            executor.record(Message.class, message);
        }
    }
}
