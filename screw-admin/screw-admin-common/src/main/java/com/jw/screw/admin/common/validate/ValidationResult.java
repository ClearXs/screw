package com.jw.screw.admin.common.validate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 验证结果
 * @author jiangw
 * @date 2020/11/20 13:45
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ValidationResult {

    /**
     * 验证后的结果
     */
    private boolean isValidate;

    /**
     * 验证失败或成功，这个字段存放相关消息
     */
    private String message;
}