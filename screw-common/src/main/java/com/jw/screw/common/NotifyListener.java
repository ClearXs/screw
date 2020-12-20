package com.jw.screw.common;

import com.jw.screw.common.metadata.RegisterMetadata;

public interface NotifyListener {

    /**
     * 消费者订阅通知
     * <p>
     *     解析回调的服务发布的地址。
     * </p>
     * <p>
     *     如果是通知添加服务：据地址创建或者获取一个通道组.创建通道数，如果通道创建成功，则添加这个通道组
     * </p>
     * <p>
     *     如果是通知移除服务，那么找到之前订阅的服务地址，进行删除
     * </p>
     * @param event
     * @param registerInfos
     */
    void notify(NotifyEvent event, RegisterMetadata... registerInfos);

    enum NotifyEvent {

        /**
         * 订阅服务添加
         */
        ADD,

        /**
         * 订阅服务的移除
         */
        REMOVED
    }
}
