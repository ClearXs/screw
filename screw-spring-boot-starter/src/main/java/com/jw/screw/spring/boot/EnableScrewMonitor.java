package com.jw.screw.spring.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启动服务时作为监控者启动
 * @author jiangw
 * @date 2021/1/9 11:42
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(BootstrapSelector.class)
public @interface EnableScrewMonitor {

    /**
     * 注册中心host
     */
    String registryAddress() default "localhost";

    /**
     * 注册中心port
     */
    int registryPort() default 8501;

    /**
     * monitor地址
     */
    String monitorAddress() default "";

    /**
     * 服务权重
     */
    int weight() default 4;

    /**
     * 可连接数
     */
    int connCount() default 10;
}
