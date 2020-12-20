package com.jw.screw.common.transport.body;

/**
 * 失败body
 * @author jiangw
 * @date 2020/12/10 17:30
 * @since 1.0
 */
public class FailBody implements Body {

    private Throwable cause;

    public FailBody(Throwable cause) {
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }
}
