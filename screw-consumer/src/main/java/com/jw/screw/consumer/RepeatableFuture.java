package com.jw.screw.consumer;

import com.jw.screw.common.future.AbstractInvokeFuture;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 可重复通知的future
 * @author jiangw
 * @date 2020/12/8 16:26
 * @since 1.0
 */
public class RepeatableFuture<V> extends AbstractInvokeFuture<V> {

    private final Class<?> realClass;

    private String providerKey;

    private String methodName;

    private Class<?>[] parameterTypes;

    private String serviceName;

    public RepeatableFuture(Class<?> realClass) {
        this(realClass, null, null, null, null);
    }

    public RepeatableFuture(Class<?> realClass, String providerKey, String serviceName, String methodName, Class<?>[] parameterTypes) {
        super();
        this.realClass = realClass;
        this.providerKey = providerKey;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public Class<?> realClass() {
        return realClass;
    }

    @Override
    public V getResult() throws ExecutionException, InterruptedException, TimeoutException {
        return (V) realClass.cast(getResult(30000));
    }

    @Override
    public V getResult(long millis) throws InterruptedException, ExecutionException, TimeoutException {
        return (V) realClass.cast(get(millis, TimeUnit.MILLISECONDS));
    }

    public void submit() {
        taskExecutor.submit(this);
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }

    public void setCallable(Callable<V> callable) {
        this.callable = callable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepeatableFuture<?> that = (RepeatableFuture<?>) o;
        return argEquals(that.providerKey, that.serviceName, that.methodName);
    }

    public boolean argEquals(String providerKey, String serviceName, String methodName) {
        return Objects.equals(providerKey, this.providerKey) && Objects.equals(methodName, this.methodName) && Objects.equals(serviceName, this.serviceName);
    }

    public boolean argEquals(String providerKey, String serviceName, String methodName, Class<?>[] parameterTypes) {
        return argEquals(providerKey, serviceName, methodName) && Arrays.equals(parameterTypes, this.parameterTypes);
    }


    @Override
    public int hashCode() {
        int result = Objects.hash(providerKey, methodName, serviceName);
        result = 31 * result + Arrays.hashCode(parameterTypes);
        return result;
    }
}
