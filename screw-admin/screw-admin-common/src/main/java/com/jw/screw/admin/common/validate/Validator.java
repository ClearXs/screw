package com.jw.screw.admin.common.validate;

import com.jw.screw.admin.common.exception.BasicOperationException;

import java.lang.reflect.InvocationTargetException;

/**
 * TDD
 * @author jiangw
 * @date 2020/11/18 17:47
 * @since 1.0
 */
public interface Validator {

    /**
     * 校验器，用于数据库存在，请求参数等校验
     * @return true 校验成功，false 校验失败
     */
    ValidationResult validate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, BasicOperationException;
}
