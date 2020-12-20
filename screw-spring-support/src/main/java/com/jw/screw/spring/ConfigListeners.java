package com.jw.screw.spring;

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
     * @param configKey
     * @param <V>
     * @return
     */
    @Deprecated
    public static <V> RepeatableFuture<V> onWatchConfig(String configKey) {
        return onWatch(configKey, "config", "configChange", null);
    }

    /**
     * 监听配置添加事件
     * @param configKey
     * @param <V>
     * @return
     */
    public static <V> RepeatableFuture<V> onAdd(String configKey) {
        return onWatch(configKey, "ConfigNotifier", "onAdd", String.class);
    }

    /**
     * 监听配置删除事件
     * @param configKey
     * @param <V>
     * @return
     */
    public static <V> RepeatableFuture<V> onDelete(String configKey) {
        return onWatch(configKey, "ConfigNotifier", "onDelete", String.class);
    }

    /**
     * 监听配置更新事件
     * @param configKey
     * @param <V>
     * @return
     */
    public static <V> RepeatableFuture<V> onUpdate(String configKey) {
        return onWatch(configKey, "ConfigNotifier", "onUpdate", String.class);
    }
}
