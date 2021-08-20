package com.jw.screw.storage;

import com.jw.screw.common.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * <b>storage 执行器管家</b>
 * <s><p>executor以每一个实体管理。遇到多个实体，就需要创建多个executor。</p></s>
 * <s><p>避免客户端管理大量的executor，所以创建一个管家类，管理这些executor</p></s>
 * @author jiangw
 * @date 2021/7/23 14:51
 * @since 1.1
 */
public class ExecutorHousekeeper {

    /**
     * <b>采取线程安全的map作为{@link Executor}的缓存</b>
     * <p>key：继承自{@link Executor}的class对象</p>
     * <p>value：一个Map，在每一个{@link Executor}的构造方法可能不一样。采取map作为存储，其中key为{@link Constructor}，value才是真正的单实例的Executor</p>
     */
    private static Map<Class<? extends Executor>,
            Map<Constructor<? extends Executor>, Executor>> executors = new ConcurrentHashMap<>();

    private final static Lock LOCK = new ReentrantLock();

    /**
     * 创建{@link com.jw.screw.storage.BaseRecoderExecutor}
     * @see #getExecutor(Class, Object...)
     */
    public static Executor getExecutor() {
        return getExecutor(BaseRecoderExecutor.class);
    }

    /**
     * 创建有参的{@link com.jw.screw.storage.BaseRecoderExecutor}
     * @see #getExecutor(Class, Object...)
     */
    public static Executor getExecutor(Object... args) {
        return getExecutor(BaseRecoderExecutor.class, args);
    }

    /**
     * <b>创建自定义executor</b>
     * @param customizeExecutor 自定义的executor的class对象
     * @param args 构造参数
     * @return {@link Executor}实例对象
     */
    public static Executor getExecutor(Class<? extends Executor> customizeExecutor, Object...args) {
        assert customizeExecutor != null;
        LOCK.lock();
        try {
            Map<Constructor<? extends Executor>, Executor> moreExecutors = getMoreExecutorByCache(customizeExecutor);
            Constructor<? extends Executor> constructor;
            if (args == null || args.length == 0) {
                constructor = customizeExecutor.getConstructor();
            } else {
                constructor = customizeExecutor.getConstructor(ClassUtils.objectToClass(args));
            }
            Executor executor = moreExecutors.get(constructor);
            if (executor == null) {
                executor = Executors.create(customizeExecutor);
            } else {
                return executor;
            }
            moreExecutors.put(constructor, executor);
            return executor;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LOCK.unlock();
        }
        return null;
    }

    @Deprecated
    public static Executor newExecutor(Class<? extends Executor> clazz) {
        LOCK.lock();
        try {
            Executor executor = null;
            Map<Constructor<? extends Executor>, Executor> moreExecutors = getMoreExecutorByCache(clazz);
            Executor sharableExecutor = getSharableExecutor(clazz, moreExecutors);
            if (sharableExecutor != null) {
                return sharableExecutor;
            }
            try {
                Constructor<? extends Executor> constructor = clazz.getConstructor();
                executor = moreExecutors.get(constructor);
                if (executor == null) {
                    executor = Executors.create(clazz);
                    moreExecutors.put(constructor, executor);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return executor;
        } finally {
            LOCK.unlock();
        }
    }

    @Deprecated
    public static Executor newExecutor(Class<? extends Executor> clazz, Object... args) {
        LOCK.lock();
        try {
            Map<Constructor<? extends Executor>, Executor> moreExecutors = getMoreExecutorByCache(clazz);
            Executor executor = null;
            Executor sharableExecutor = getSharableExecutor(clazz, moreExecutors);
            if (sharableExecutor != null) {
                return sharableExecutor;
            }
            try {
                Class<?>[] classes = Arrays.stream(args)
                        .map(Object::getClass)
                        .collect(Collectors.toList())
                        .toArray(new Class[]{});
                Constructor<? extends Executor> constructor = clazz.getConstructor(classes);
                executor = moreExecutors.get(constructor);
                if (executor == null) {
                    executor = Executors.create(clazz, args);
                    moreExecutors.put(constructor, executor);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return executor;
        } finally {
            LOCK.unlock();
        }
    }

    private static Map<Constructor<? extends Executor>, Executor> getMoreExecutorByCache(Class<? extends Executor> clazz) {
        Map<Constructor<? extends Executor>, Executor> moreExecutors = executors.get(clazz);
        if (moreExecutors == null) {
            moreExecutors = new ConcurrentHashMap<>();
            executors.put(clazz, moreExecutors);
            return moreExecutors;
        }
        return moreExecutors;
    }

    private static Executor getSharableExecutor(Class<? extends Executor> clazz, Map<Constructor<? extends Executor>, Executor> moreExecutors) {
        Executor.Sharable sharable = clazz.getAnnotation(Executor.Sharable.class);
        if (sharable == null) {
            return null;
        }
        for (Executor value : moreExecutors.values()) {
            return value;
        }
        return null;
    }
}
