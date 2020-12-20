package com.jw.screw.admin.common.exception;

/**
 * 基础操作异常，添加、更新、删除。
 * @author jiangw
 * @date 2020/11/13 11:19
 * @since 1.0
 */
public class BasicOperationException extends Exception {

    public BasicOperationException() {
        super();
    }

    public BasicOperationException(String message) {
        super(message);
    }

    public BasicOperationException(Throwable cause) {
        super(cause);
    }
}
