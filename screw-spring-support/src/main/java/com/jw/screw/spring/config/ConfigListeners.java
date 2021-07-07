package com.jw.screw.spring.config;

import com.jw.screw.consumer.Listeners;
import com.jw.screw.consumer.RepeatableFuture;

/**
 * 针对配置中心的监听
 * @author jiangw
 * @date 2020/12/10 10:58
 * @since 1.0
 */
public class ConfigListeners extends Listeners {

    /**
     * 监听目标配置的变化
     * @param providerKey
     * @param <V>
     * @return
     */
    @Deprecated
    public static <V> RepeatableFuture<V> onWatchConfig(String providerKey) {
        return onWatch(providerKey, "config", "configChange", null);
    }

    /**
     * 监听配置添加事件
     * @param providerKey
     * @param <V>
     * @return
     */
    public static <V> RepeatableFuture<V> onAdd(String providerKey) {
        return onWatch(providerKey, "ConfigNotifier", "onAdd", String.class);
    }

    /**
     * 监听配置删除事件
     * @param providerKey
     * @param <V>
     * @return
     */
    public static <V> RepeatableFuture<V> onDelete(String providerKey) {
        return onWatch(providerKey, "ConfigNotifier", "onDelete", String.class);
    }

    /**
     * 监听配置更新事件
     * @param providerKey
     * @param <V>
     * @return
     */
    public static <V> RepeatableFuture<V> onUpdate(String providerKey) {
        return onWatch(providerKey, "ConfigNotifier", "onUpdate", String.class);
    }
}
