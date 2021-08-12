package com.jw.screw.storage;

import java.lang.annotation.*;

/**
 * <b>给某个方法取别名，让其动态的调用这个别名方法</b>
 * <p>参考自：{@link org.springframework.core.annotation.AliasFor}</p>
 * @author jiangw
 * @date 2021/7/26 11:09
 * @since 1.1
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface AliasFor {

    /**
     * 别名
     */
    String value();

    /**
     * 目标方法的类型class对象
     */
    Class<?>[] parameterTypes();

}
