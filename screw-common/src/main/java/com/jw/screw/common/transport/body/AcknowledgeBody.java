package com.jw.screw.common.transport.body;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Ack确认消息
 * @author jiangw
 * @date 2020/11/30 13:37
 * @since 1.0
 */
public class AcknowledgeBody implements Body {

    private final static AtomicLong NEXT = new AtomicLong(0);

    private final long sequence;

    /**
     * 处理是否成功
     */
    private boolean isSuccess;

    public AcknowledgeBody(boolean isSuccess) {
        this(NEXT.getAndIncrement(), isSuccess);
    }

    public AcknowledgeBody(long sequence, boolean isSuccess) {
        this.sequence = sequence;
        this.isSuccess = isSuccess;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public long getSequence() {
        return sequence;
    }
}
