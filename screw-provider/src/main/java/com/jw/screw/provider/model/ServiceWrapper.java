package com.jw.screw.provider.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 调用服务的编织类
 * @author jiangw
 * @date 2020/11/26 20:27
 * @since 1.0
 */
public class ServiceWrapper {

    /**
     * 原生服务实例
     */
    private Object serviceProvider;

    /**
     * 调用的服务名
     */
    private String serviceName;

    /**
     * 方法对应的编织map
     */
    private List<MethodWrapper> methodWrappers;

    public ServiceWrapper(Object serviceProvider, String serviceName) {
        this.serviceProvider = serviceProvider;
        this.serviceName = serviceName;
    }

    public Object getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(Object serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void addMethodWrapper(String methodName, Class<?>[] parameterTypes) {
        if (methodWrappers == null) {
            methodWrappers = new ArrayList<>();
        }
        methodWrappers.add(new MethodWrapper(methodName, parameterTypes));
    }

    public List<MethodWrapper> getMethodWrappers() {
        return methodWrappers;
    }

    /**
     * 对于服务来说，它可以存在被标识为rpc调用的方法
     */
    public static class MethodWrapper {

        /**
         * 方法名
         */
        private String methodName;

        /**
         * 方法参数
         */
        private Class<?>[] parameterTypes;

        public MethodWrapper(String methodName, Class<?>[] parameterTypes) {
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
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

        public boolean equals(String methodName, Object[] parameters) {
            boolean methodEquals = Objects.equals(this.methodName, methodName);
            if (parameters != null) {
                return methodEquals &&
                        this.parameterTypes.length == parameters.length;
            } else {
                return methodEquals;
            }
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(methodName);
            result = 31 * result + Arrays.hashCode(parameterTypes);
            return result;
        }
    }
}
