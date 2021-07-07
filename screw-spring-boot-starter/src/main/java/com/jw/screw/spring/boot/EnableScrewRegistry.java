package com.jw.screw.spring.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启动服务时作为注册中心启动
 * @author jiangw
 * @date 2021/1/9 11:43
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(BootstrapSelector.class)
public @interface EnableScrewRegistry {

    /**
     * 注册中心端口
     */
    int registryPort() default 8501;
}
