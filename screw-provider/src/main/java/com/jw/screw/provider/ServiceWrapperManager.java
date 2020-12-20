package com.jw.screw.provider;

import com.jw.screw.provider.annotations.ProviderService;
import com.jw.screw.provider.model.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 对需要发布的服务进行编织。
 * 1.提供发布服务的类
 * 2.对发布的类进行解析
 * 3.加入wrapper容器中进行管理
 * @author jiangw
 * @date 2020/11/26 20:55
 * @since 1.0
 */
public class ServiceWrapperManager {


    private final static String NOTIFY = "notify";
    private final static String NOTIFY_ALL = "notifyAll";
    private final static String EQUALS = "equals";
    private final static String TO_STRING = "toString";
    private final static String HASH_CODE = "hashCode";
    private final static String INDEX_OF = "indexOf";
    private final static String NEW_INSTANCE = "newInstance";
    private final static String IS_FROZEN = "isFrozen";
    private final static String IS_INTERFACE_PROXIED = "isInterfaceProxied";
    private final static String TO_PROXY_CONFIG_STRING = "toProxyConfigString";
    private final static String IS_PROXY_TARGET_CLASS = "isProxyTargetClass";
    private final static String WAIT = "wait";
    private final static String GET_CLASS = "getClass";
    private final static String SET_CALLBACK = "setCallback";
    private final static String SET_CALLBACKS = "setCallbacks";
    private final static String GET_CALLBACK = "getCallback";
    private final static String GET_CALLBACKS = "getCallbacks";
    private final static String IS_EXPOSE_PROXY = "isExposeProxy";
    private final static String SET_TARGET_SOURCE = "setTargetSource";
    private final static String GET_ADVISORS = "getAdvisors";
    private final static String GET_TARGET_SOURCE = "getTargetSource";
    private final static String IS_PRE_FILTERED = "isPreFiltered";
    private final static String SET_EXPOSE_PROXY = "setExposeProxy";
    private final static String GET_TARGET_CLASS = "getTargetClass";
    private final static String GET_PROXIED_INTERFACES = "getProxiedInterfaces";

    /**
     * 对于那些{@link #notify()}、{@link #clone()}等方法则排除
     */
    private final static List<String> EXCLUDE_METHOD = new ArrayList<>();

    static {
        EXCLUDE_METHOD.add(NOTIFY);
        EXCLUDE_METHOD.add(NOTIFY_ALL);
        EXCLUDE_METHOD.add(EQUALS);
        EXCLUDE_METHOD.add(TO_STRING);
        EXCLUDE_METHOD.add(HASH_CODE);
        EXCLUDE_METHOD.add(INDEX_OF);
        EXCLUDE_METHOD.add(NEW_INSTANCE);
        EXCLUDE_METHOD.add(IS_FROZEN);
        EXCLUDE_METHOD.add(IS_INTERFACE_PROXIED);
        EXCLUDE_METHOD.add(TO_PROXY_CONFIG_STRING);
        EXCLUDE_METHOD.add(IS_PROXY_TARGET_CLASS);
        EXCLUDE_METHOD.add(WAIT);
        EXCLUDE_METHOD.add(GET_CLASS);
        EXCLUDE_METHOD.add(SET_CALLBACK);
        EXCLUDE_METHOD.add(SET_CALLBACKS);
        EXCLUDE_METHOD.add(GET_CALLBACK);
        EXCLUDE_METHOD.add(GET_CALLBACKS);
        EXCLUDE_METHOD.add(IS_EXPOSE_PROXY);
        EXCLUDE_METHOD.add(SET_TARGET_SOURCE);
        EXCLUDE_METHOD.add(GET_ADVISORS);
        EXCLUDE_METHOD.add(GET_TARGET_SOURCE);
        EXCLUDE_METHOD.add(IS_PRE_FILTERED);
        EXCLUDE_METHOD.add(SET_EXPOSE_PROXY);
        EXCLUDE_METHOD.add(GET_TARGET_CLASS);
        EXCLUDE_METHOD.add(GET_PROXIED_INTERFACES);
    }

    private static Logger logger = LoggerFactory.getLogger(ServiceWrapperManager.class);

    private final ServiceWrapperContainer wrapperContainer;

    public ServiceWrapperManager() {
        wrapperContainer = new ServiceWrapperContainer();
    }

    /**
     * 对需要发布的服务加入容器中
     * @param publishServices
     * @return
     */
    public void register(Object... publishServices) {
        for (Object publishService : publishServices) {
            ServiceWrapper serviceWrapper = create(publishService);
            if (serviceWrapper != null) {
                wrapperContainer.registerWrapper(serviceWrapper.getServiceName(), serviceWrapper);
            }
        }
    }

    public ServiceWrapperContainer wrapperContainer() {
        return wrapperContainer;
    }

    private ServiceWrapper create(Object publishService) {
        // 解析发布的服务
        Class<?> serviceClass = publishService.getClass();
        Method[] methods = serviceClass.getMethods();
        ProviderService service = serviceClass.getAnnotation(ProviderService.class);
        if (service == null) {
            return null;
        }
        ServiceWrapper serviceWrapper;
        Class<?> publishClass = service.publishService();
        String serviceName = publishClass.getSimpleName();
        serviceWrapper = new ServiceWrapper(publishService, serviceName);
        for (Method method : methods) {
            Class<?> declaringClass = method.getDeclaringClass();
            Class<?>[] declaringClassInterfaces = declaringClass.getInterfaces();
            boolean isPublishMethod = false;
            // 判断当实例的所实现的接口类型是否是发布的接口类型
            // 比如发布DemoService，如果当前实例没有实现，那么就排除
            for (Class<?> anInterface : declaringClassInterfaces) {
                if (anInterface == publishClass) {
                    isPublishMethod = true;
                    break;
                }
            }
            String methodName = method.getName();
            // 排除那些不需要添加的方法
            if (EXCLUDE_METHOD.contains(methodName)) {
                continue;
            }
            if (!isPublishMethod) {
                // 如果使用cglib代理，那么方法所在的类上会存在注解丢失、实现的接口丢失，那么需要再次对注解进行判断
                ProviderService providerService = declaringClass.getAnnotation(ProviderService.class);
                if (providerService != null) {
                    isPublishMethod = true;
                }
                if (!isPublishMethod) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("{} can't join @ProviderService, cannot add publish service", methodName);
                    }
                    continue;
                }
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            serviceWrapper.addMethodWrapper(methodName, parameterTypes);
            if (logger.isDebugEnabled()) {
                logger.debug("{} - {}: wrapper successful", serviceName, methodName);
            }
        }
        return serviceWrapper;
    }

}
