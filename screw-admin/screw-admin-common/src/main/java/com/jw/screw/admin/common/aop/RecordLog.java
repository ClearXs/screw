package com.jw.screw.admin.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

/**
 * 记录接口调用时长
 * @author jiangw
 * @date 2020/11/10 18:23
 * @since 1.0
 */
@Configuration
@Aspect
@Slf4j
public class RecordLog {

    @Pointcut("execution(* com.jw.screw.admin.web.controller..*(..))")
    public void pointCut() {};

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object object;
        String method = point.getTarget().getClass().getName() + "." + point.getSignature().getName();
        long startTime = System.currentTimeMillis();
        object = point.proceed();
        long endTime = System.currentTimeMillis();
        log.info("===>[{}] 执行时间：[{}ms]", method, (endTime - startTime));
        return object;
    }
}
