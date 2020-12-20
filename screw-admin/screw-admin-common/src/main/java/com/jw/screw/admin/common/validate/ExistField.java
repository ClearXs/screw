package com.jw.screw.admin.common.validate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jiangw
 * @date 2020/11/20 13:53
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class ExistField {

    /**
     * 对于与数据的字段，即filed_name形式
     */
    String dataBaseFiled;

    /**
     * 这个字段对应的值
     */
    Object value;

    /**
     * 对应于{@link Exist}的rule注解
     */
    String rule;

    /**
     * 对应于{@link Exist}的id注解
     */
    boolean id;

    /**
     * 对于于{@link Exist}的extension注解
     */
    boolean extension;

    /**
     * 校验失败的信息
     */
    String errorMessage;
}
