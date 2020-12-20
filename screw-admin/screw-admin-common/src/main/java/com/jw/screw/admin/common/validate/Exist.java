package com.jw.screw.admin.common.validate;

import com.jw.screw.admin.common.constant.StringPool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jiangw
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Exist {

    /**
     * 标识当前字段是否是id，若标识那么当前字段主要用来过滤当前数据
     * 比如说，在更新的时候排除数据库中当前的数据
     * 即创建id != '123'的语句。被标识的字段必须有值，否则不会生效
     */
    boolean id() default false;

    /**
     * 标识字段是否需要判断唯一性
     */
    String unique() default "";

    /**
     * 拼接的规则 =、!=、in...
     */
    String rule() default StringPool.EQUALS;

    /**
     * 校验不通过的错误信息，如果有${value}，则取上当前字段的value
     */
    String errorMessage() default "";

    /**
     * 对于存在性的判断可能存在在某个范围内判断
     * 被标识的字段，将会按照equals进行规则连接，并且必须有值.
     * 注意当id和extension一起使用时只有id生效
     */
    boolean extension() default false;
}
