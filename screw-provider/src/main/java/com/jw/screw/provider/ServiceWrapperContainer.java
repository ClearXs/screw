package com.jw.screw.provider;

import com.jw.screw.provider.model.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 存放服务的容器类
 * @author jiangw
 * @date 2020/11/26 20:35
 * @since 1.0
 */
public class ServiceWrapperContainer implements WrapperContainer<ServiceWrapper> {

    private static Logger logger = LoggerFactory.getLogger(ServiceWrapperContainer.class);

    private final ConcurrentHashMap<String, ServiceWrapper> wrappers = new ConcurrentHashMap<>();

    private final LinkedBlockingQueue<ServiceWrapper> wrapperList = new LinkedBlockingQueue<>();

    @Override
    public void registerWrapper(String name, ServiceWrapper wrapper) {
        if (logger.isDebugEnabled()) {
            logger.debug("register service wrapper {}", wrapper);
        }
        wrappers.put(name, wrapper);
        wrapperList.add(wrapper);
    }

    @Override
    public ServiceWrapper lookupWrapper(String serviceName) {
        return wrappers.get(serviceName);
    }

    @Override
    public LinkedBlockingQueue<ServiceWrapper> wrappers() {
        return wrapperList;
    }

}
