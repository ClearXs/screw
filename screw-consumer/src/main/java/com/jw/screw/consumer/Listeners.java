package com.jw.screw.consumer;

import com.jw.screw.common.transport.body.MonitorBody;
import com.jw.screw.common.util.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 监听远程服务是否有消息更新
 * @author jiangw
 * @date 2020/12/8 15:22
 * @since 1.0
 */
public class Listeners {

    private static Logger logger = LoggerFactory.getLogger(Listeners.class);

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final CopyOnWriteArraySet<RepeatableFuture<?>> FUTURE_GROUP = new CopyOnWriteArraySet<>();

    /**
     * 产生一个future对象，消费者与服务提供者不必要同一个api
     * @param providerKey 服务提供者的key
     * @param serviceName 服务提供者的的服务名称，简单名称。如DemoService
     * @param targetMethodName 服务提供者方法，简单名称，如hello
     * @param parameterTypes 建议加上调用目标方法的参数类型
     * @param <V> 目标的结果泛型
     * @return {@link #attach(String, Class, String, String, Class[])}
     */
    public static <V> RepeatableFuture<V> onWatch(String providerKey, String serviceName, String targetMethodName,
                                                  Class<?>... parameterTypes) {
        Requires.isNull(providerKey, "providerKey");
        Requires.isNull(serviceName, "serviceName");
        Requires.isNull(targetMethodName, "methodName");
        return attach(providerKey, Object.class, serviceName, targetMethodName, parameterTypes);
    }

    /**
     * @see #onWatch(String, Class, String, Class[])
     */
    public static <V> RepeatableFuture<V> onWatch(String providerKey, Class<?> targetClass, String targetMethodName,
                                                Class<?>... parameterTypes) throws NoSuchMethodException {
        Requires.isNull(targetMethodName, "methodName");
        Method method = targetClass.getMethod(targetMethodName, parameterTypes);
        return onWatch(providerKey, targetClass, method);
    }

    /**
     * 产生一个future对象，消费者与服务提供者必须拥有同一个api。
     * @param providerKey 服务提供者的key
     * @param targetClass 调用的目标的class对象 如DemoService.class
     * @param targetMethod 调用的目标方法
     * @param <V> 目标的结果泛型
     * @return {@link #attach(String, Class, String, String, Class[])}
     * @throws NoSuchMethodException 如果targetMethod不在targetClass抛出异常
     */
    public static <V> RepeatableFuture<V> onWatch(String providerKey, Class<?> targetClass, Method targetMethod) throws NoSuchMethodException {
        Requires.isNull(providerKey, "providerKey");
        Requires.isNull(targetMethod, "method");
        // 验证targetClass与targetMethod
        Method method = targetClass.getMethod(targetMethod.getName(), targetMethod.getParameterTypes());
        Requires.isNull(method, "target method");
        if (!method.equals(targetMethod)) {
            throw new NoSuchMethodException("target not in class");
        }
        return attach(providerKey, targetMethod.getReturnType(), targetClass.getSimpleName(),
                targetMethod.getName(), targetMethod.getParameterTypes());
    }

    private static <V> RepeatableFuture<V> attach(String providerKey, Class<?> returnType, String serviceName,
                                                  String methodName, Class<?>[] parameterTypes) {
        LOCK.lock();
        try {
            RepeatableFuture<V> repeatableFuture = new RepeatableFuture<V>(returnType);
            repeatableFuture.setProviderKey(providerKey);
            repeatableFuture.setServiceName(serviceName);
            repeatableFuture.setMethodName(methodName);
            repeatableFuture.setParameterTypes(parameterTypes);
            FUTURE_GROUP.add(repeatableFuture);
            return repeatableFuture;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 取消观测
     */
    public static void unWatch() {
        FUTURE_GROUP.clear();
    }

    /**
     * 通知与monitor一致的watcher
     * @param monitorBody {@link MonitorBody}
     * @throws ClassNotFoundException
     */
    public static void notifyFuture(final MonitorBody monitorBody) throws ClassNotFoundException {
        LOCK.lock();
        try {
            Class<?> targetClass = null;
            try {
                targetClass = Class.forName(monitorBody.getServiceName());
            } catch (ClassNotFoundException e) {
                logger.debug("can't create class: {}", monitorBody.getServiceName());
            }
            String serviceName = targetClass == null ? monitorBody.getServiceName()
                    : targetClass.getSimpleName();
            MonitorBody.MethodWrapper methodWrapper = monitorBody.getMethodWrapper();
            try {
                RepeatableFuture<?> repeatableFuture = null;
                for (RepeatableFuture<?> future : FUTURE_GROUP) {
                    if (future.getParameterTypes() != null && methodWrapper.getParameterTypes() != null) {
                        if (future.argEquals(monitorBody.getProviderKey(), serviceName,
                                methodWrapper.getName(), methodWrapper.getParameterTypes())) {
                            repeatableFuture = future;
                        }
                    } else {
                        if (future.argEquals(monitorBody.getProviderKey(), serviceName, methodWrapper.getName())) {
                            repeatableFuture = future;
                        }
                    }
                }
                if (repeatableFuture != null) {
                    // 进行通知
                    repeatableFuture.setCallable(new Callable() {
                        @Override
                        public Object call() throws Exception {
                            return monitorBody.getResult();
                        }
                    });
                    repeatableFuture.submit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            LOCK.unlock();
        }
    }
}
