package com.jw.screw.common.transport.body;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 请求body
 * @author jiangw
 * @date 2020/12/10 17:30
 * @since 1.0
 */
public class RequestBody implements Body {

    private final static AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    /**
     * 调用唯一id，以此对应消费者与提供者之间的请求
     */
    private final long invokeId;

    /**
     * 服务名
     */
    private String serviceName;

    /**
     * 调用的方法名
     */
    private String methodName;

    /**
     * 请求的参数
     */
    private Object[] parameters;

    public RequestBody() {
        invokeId = ID_GENERATOR.getAndIncrement();
    }

    public long getInvokeId() {
        return invokeId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "RequestBody{" +
                "invokeId=" + invokeId +
                ", serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameters=" + Arrays.toString(parameters) +
                '}';
    }
}
