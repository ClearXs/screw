package com.jw.screw.spring.anntation;

import java.lang.annotation.*;

/**
 * 类似于{@link org.springframework.beans.factory.annotation.Value}
 * <p>
 *     规则：配置文件名-具体配置名称
 * </p>
 *  <code>
 *      #@ScrewValue("screw-properties.name")
 *      private int test
 *  </code>
 * @author jiangw
 * @date 2020/12/9 17:53
 * @since 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ScrewValue {

    String value() default "";
}
