package com.jw.screw.admin.common.validate;

/**
 * 数据库操作结构的判断，比如更新、插入的操作
 * @author jiangw
 * @date 2020/11/20 10:56
 * @since 1.0
 */
class DataOperationResultValidator implements Validator {

    private final int result;

    public DataOperationResultValidator(int result) {
        this.result = result;
    }

    @Override
    public ValidationResult validate() {
        return new ValidationResult(result > 0, "数据库操作失败");
    }
}
