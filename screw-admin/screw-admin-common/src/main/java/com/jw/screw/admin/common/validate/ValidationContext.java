package com.jw.screw.admin.common.validate;

import com.jw.screw.admin.common.exception.BasicOperationException;

import java.lang.reflect.InvocationTargetException;

/**
 * 协调构造器，或者其他资源的上下文对象
 * @author jiangw
 * @date 2020/11/20 10:45
 * @since 1.0
 */
class ValidationContext {

    private Validator validator;

    public ValidationContext() {

    }

    public ValidationContext(Validator validator) {
        this.validator = validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public ValidationResult valid() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, BasicOperationException {
        return validator.validate();
    }
}
