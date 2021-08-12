package com.jw.screw.logging.core.annotation;

import com.jw.screw.logging.core.constant.LogSource;

import java.lang.annotation.*;

/**
 * <b>通过注解在类，或者方法上，动态获取日志注解元数据</b>
 * @author jiangw
 * @date 2021/7/16 14:28
 * @since 1.1
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ScrewLog {

    /**
     * 数据来源
     * @see LogSource
     */
    String source() default LogSource.APPLICATION_NAME;

    /**
     * 日志类型，即具体的业务类型
     */
    String type() default "";
}
