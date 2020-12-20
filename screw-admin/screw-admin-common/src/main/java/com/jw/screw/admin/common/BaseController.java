package com.jw.screw.admin.common;

import com.jw.screw.admin.common.constant.DataOperationState;
import com.jw.screw.admin.common.exception.BasicOperationException;
import org.springframework.http.HttpStatus;

/**
 * screw
 * @author jiangw
 * @date 2020/11/9 17:48
 * @since 1.0
 */
public class BaseController {

    protected <T> MsgResponse<T> getSuccessResponse(String message, T data) {
        return getResponse(true, HttpStatus.OK.value(), message, data);
    }

    protected <T> MsgResponse<T> getExceptionResponse(Exception e) {
        return getExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e);
    }

    protected <T> MsgResponse<T> getExceptionResponse(int code, Exception e) {
        e.printStackTrace();
        return new MsgResponse<T>(false, code, e.getMessage());
    }

    protected <T> MsgResponse<T> getResponse(boolean isSuccess, int code, String message, T data) {
        return new MsgResponse<T>(isSuccess, code, message, data);
    }

    /**
     * 处理数据库的添加、更新、删除的返回结果，如果不是1的话，那么抛出操作执行失败的异常
     * @author jiangw
     * @date 2020/11/13 11:20
     * @since 1.0
     */
    protected <T> MsgResponse<T> handleBasicOperationResponse(String message, Integer success) {
        if (success >= DataOperationState.SUCCESSFUL) {
            return (MsgResponse<T>) getSuccessResponse(message, success);
        } else {
            return getExceptionResponse(new BasicOperationException("操作失败"));
        }
    }
}
