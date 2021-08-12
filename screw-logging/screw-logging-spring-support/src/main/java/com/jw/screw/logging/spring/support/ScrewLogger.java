package com.jw.screw.logging.spring.support;

import com.alibaba.fastjson.JSON;
import com.jw.screw.logging.core.annotation.ScrewLog;
import com.jw.screw.logging.core.constant.LogSource;
import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.Executor;
import com.jw.screw.storage.ExecutorHousekeeper;
import com.jw.screw.storage.properties.StorageProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于spring配置
 * @author jiangw
 * @date 2021/7/16 15:55
 * @since 1.1
 */
@Aspect
public class ScrewLogger implements EnvironmentAware, DisposableBean {

    private Environment environment;

    /**
     * @annotation(注解类型)：匹配被调用的方法上有指定的注解。
     */
    @Pointcut("@annotation(com.jw.screw.logging.core.annotation.ScrewLog)")
    public void screwLogMethodPoint() {

    }

    /**
     * 使用@target，那么在一些对象就会被添加上对应的切面，就会造成无法生成cglib对象
     * @within(注解类型)：匹配指定的注解内定义的方法。
     */
    @Pointcut("@within(com.jw.screw.logging.core.annotation.ScrewLog)")
    public void screwLogClassPoint() {

    }

    private final Executor executor;

    public ScrewLogger() {
        executor = ExecutorHousekeeper.getExecutor();
    }

    public ScrewLogger(StorageProperties properties) {
        executor = ExecutorHousekeeper.getExecutor(properties);
    }

    @Override
    public void destroy() throws Exception {
        executor.close();
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Around("screwLogMethodPoint()")
    public Object injectMethod(ProceedingJoinPoint point) throws Throwable {
        return inject(point, true);
    }

    @Around("screwLogClassPoint()")
    public Object injectClass(ProceedingJoinPoint point) throws Throwable {
        // 判断方法上是否存在注解，存在就不进行注入
        String methodName = point.getSignature().getName();
        List<? extends Class<?>> argsClasses = Arrays.stream(point.getArgs())
                .map(Object::getClass)
                .collect(Collectors.toList());
        Method method = getMethod(point.getTarget(), methodName, argsClasses.toArray(new Class<?>[] { }));
        ScrewLog annotation = method.getAnnotation(ScrewLog.class);
        return inject(point, annotation == null);
    }

    private Object inject(ProceedingJoinPoint point, boolean isInject) throws Throwable {
        String methodName = point.getSignature().getName();
        List<? extends Class<?>> argsClasses = Arrays.stream(point.getArgs())
                .map(Object::getClass)
                .collect(Collectors.toList());
        Object proceed = null;
        Object content = null;
        try {
            proceed = point.proceed();
            content = proceed;
        } catch (Exception e) {
            content = e.getStackTrace();
        } finally {
            Message message = analysisBean(point.getTarget(), methodName, argsClasses.toArray(new Class<?>[]{ }));
            if (message != null && isInject) {
                message.setContent(JSON.toJSONString(content));
                executor.record(Message.class, message);
            }
        }
        return proceed;
    }

    /**
     * <b>分析目标bean是否存在{@link com.jw.screw.logging.core.annotation.ScrewLog}注解</b>
     * <p>1.类上存在该注解，不进行方法分析</p>
     * <p>2.类上没有存在该注解，对方法进行分析</p>
     * <p>note：如果类上与方法上添加该注解，以方法为最终结果</p>
     * @param target 目标对象的Class
     * @param methodName 调用目标对象方法名称
     * @return 如果分析结果存在注解，那么返回实例{@link Message}对象
     */
    private Message analysisBean(Object target, String methodName, Class<?>... args) {
        ScrewLog classAnnotation = target.getClass().getAnnotation(ScrewLog.class);
        // 分析方法
        ScrewLog methodAnnotation = null;
        Method method = getMethod(target, methodName, args);
        methodAnnotation = method.getAnnotation(ScrewLog.class);
        if (methodAnnotation != null) {
            Message message = new Message();
            String source = methodAnnotation.source();
            if (LogSource.APPLICATION_NAME.equals(source)) {
                source = environment.getProperty("spring.application.name");
            }
            message.setSource(source);
            message.setType(methodAnnotation.type());
            return message;
        }
        if (classAnnotation != null) {
            Message message = new Message();
            String source = classAnnotation.source();
            if (LogSource.APPLICATION_NAME.equals(source)) {
                source = environment.getProperty("spring.application.name");
            }
            message.setSource(source);
            message.setType(classAnnotation.type());
            return message;
        }
        return null;
    }

    private static Method getMethod(Object target, String methodName, Class<?>... args) {
        Assert.notNull(target, "target object is empty, can't get target method");
        // 判断方法上是否存在注解，存在就不进行注入
        Method method = null;
        try {
            method = target.getClass().getMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            // 出现参数类型错误时将抛出异常，在异常中，不做参数匹配，采取名称与参数数量是否一致进行判断
            for (Method declaredMethod : target.getClass().getDeclaredMethods()) {
                if (methodName.equals(declaredMethod.getName()) && declaredMethod.getParameterTypes().length == args.length) {
                    method = declaredMethod;
                }
            }
        }
        Assert.notNull(method, String.format("not found class: %s method: %s", target.getClass().getName(), methodName));
        return method;
    }
}

