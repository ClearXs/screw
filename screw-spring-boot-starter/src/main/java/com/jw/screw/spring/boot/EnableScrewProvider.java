package com.jw.screw.spring.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启动服务时作为提供者启动
 * @author jiangw
 * @date 2021/1/9 11:43
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(BootstrapSelector.class)
public @interface EnableScrewProvider {

    /**
     * 是否启用
     */
    boolean enable() default true;

    /**
     * 服务key
     */
    String serverKey();

    /**
     * 提供者地址
     */
    String providerAddress() default "";

    /**
     * 提供者port 需要与tomcat jetty等服务器port区分
     */
    int serverPort();

    /**
     * 注册中心host
     */
    String registryAddress() default "localhost";

    /**
     * 注册中心端口
     */
    int registryPort() default 8501;

    /**
     * 服务权重
     */
    int weight() default 4;

    /**
     * 处理rpc请求的核心线程数
     */
    int connCount() default 10;

    /**
     * 监控中心
     */
    String monitorKey() default "";

    /**
     * 监控指标收集周期 unit second
     */
    int monitorCollectPeriod() default 10;

    /**
     * 发布服务包所在位置
     */
    String packageScan() default "";

    /**
     * 当前服务是否是配置中心
     */
    boolean isConfigCenter() default false;
}
