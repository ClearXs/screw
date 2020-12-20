package com.jw.screw.admin.common.validate;

import com.jw.screw.admin.common.exception.BasicOperationException;

import java.lang.reflect.InvocationTargetException;

/**
 * @author jiangw
 * @date 2020/11/20 13:31
 * @since 1.0
 */
public class Validators {

    private static final ValidationContext CONTEXT = new ValidationContext();

    public static ValidationResult doExist(ExistMapper existMapper, Object entity) throws BasicOperationException {
        Existing existing = new Existing(existMapper, entity);
        ExistValidator validator = new ExistValidator(existing);
        CONTEXT.setValidator(validator);
        return doValidate();
    }

    public static ValidationResult doResult(int result) throws BasicOperationException {
        DataOperationResultValidator validator = new DataOperationResultValidator(result);
        CONTEXT.setValidator(validator);
        return doValidate();
    }

    private static ValidationResult doValidate() throws BasicOperationException {
        ValidationResult validationResult;
        try {
            validationResult = CONTEXT.valid();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new BasicOperationException(e);
        }
        if (!validationResult.isValidate()) {
            throw new BasicOperationException(validationResult.getMessage());
        }
        return validationResult;
    }
}
