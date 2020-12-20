package com.jw.screw.provider;

import com.jw.screw.common.util.Collections;
import com.jw.screw.provider.model.ServiceWrapper;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * screw
 * @author jiangw
 * @date 2020/12/10 14:13
 * @since 1.0
 */
public abstract class AbstractRpcRequestProcessor implements NettyProcessor {

    private static Logger logger = LoggerFactory.getLogger(AbstractRpcRequestProcessor.class);

    /**
     * rpc调用生成结果核心方法
     * @param serviceWrapper
     * @return
     */
    protected Object rpcInvokeGenerate(ServiceWrapper serviceWrapper, String methodName, Object[] parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<ServiceWrapper.MethodWrapper> methodWrappers = serviceWrapper.getMethodWrappers();
        if (Collections.isEmpty(methodWrappers)) {
            if (logger.isWarnEnabled()) {
                logger.warn("methodWrappers is empty. {}", serviceWrapper);
            }
            throw new NoSuchMethodException("methodWrappers is empty");
        }
        for (ServiceWrapper.MethodWrapper methodWrapper : methodWrappers) {
            if (methodWrapper.equals(methodName, parameters)) {
                Object serviceProvider = serviceWrapper.getServiceProvider();
                Class<?>[] parameterTypes = methodWrapper.getParameterTypes();
                Class<?> providerClass = serviceProvider.getClass();
                Method method = providerClass.getMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method.invoke(serviceProvider, parameters);
            }
        }
        if (logger.isWarnEnabled()) {
            logger.warn("method not found by service name: {}, and method name: {}", serviceWrapper.getServiceName(), methodName);
        }
        throw new NoSuchMethodException("method not found");
    }
}
