package com.jw.screw.provider;


import com.jw.screw.provider.model.ServiceWrapper;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 编织的公共容器接口
 * 1.提供注册
 * 2.查找
 * @author jiangw
 * @date 2020/11/26 20:38
 * @since 1.0
 */
public interface WrapperContainer<T> {

    /**
     * 子类通过map进行存储这个wrapper
     * @param name service name
     * @param wrapper T
     */
    void registerWrapper(String name, T wrapper);

    /**
     * 查找这个wrapper
     * @param name service name
     * @return T
     */
    T lookupWrapper(String name);

    /**
     * 返回已经获取的所有wrapper，list进行封装
     * @return {@link LinkedBlockingQueue<ServiceWrapper>}
     */
    LinkedBlockingQueue<ServiceWrapper> wrappers();
}
