package com.jw.screw.spring.boot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启动当前服务作为消费者的注册
 * @author jiangw
 * @date 2021/1/8 16:33
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(BootstrapSelector.class)
public @interface EnableScrewConsumer {

    /**
     * 是否启用
     */
    boolean enable() default true;

    /**
     * 服务key
     */
    String serverKey();

    /**
     * 服务端口，如果当前服务作为provider那么作为consumer的端口一定与其相同。
     */
    int serverPort() default 8080;

    /**
     * 注册中心host
     */
    String registryAddress() default "localhost";

    /**
     * 注册中心port
     */
    int registryPort() default 8501;

    /**
     * 连接等待时长
     */
    long waitMills() default 30000;

    /**
     * 负载均衡
     */
    String loadBalance() default "";

    /**
     * 配置中心key，如果存在则填写
     */
    String configKey() default "";

    /**
     * 监控中心key，如果存在则填写
     */
    String monitorKey() default "";

    /**
     * 监控指标收集周期 unit Second
     */
    int monitorCollectPeriod() default 10;
}
