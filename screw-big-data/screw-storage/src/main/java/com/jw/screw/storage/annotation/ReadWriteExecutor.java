package com.jw.screw.storage.annotation;

import java.lang.annotation.*;

/**
 *
 * @author jiangw
 * @date 2021/7/22 9:23
 * @since 1.1
 */
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ReadWriteExecutor {

    /**
     * 注解于方法上，标识当前方法只读数据
     */
    boolean read() default false;

    /**
     * 注解于方法上，标识当前方法只写数据
     */
    boolean write() default false;

}
