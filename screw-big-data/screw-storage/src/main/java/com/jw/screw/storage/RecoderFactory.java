package com.jw.screw.storage;

import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.util.ClassUtils;
import com.jw.screw.common.util.Collections;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.Recoder;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <b>recoder创建工厂，采取读、写分离的recoder。</b>
 * @author jiangw
 * @date 2021/7/23 10:25
 * @since 1.1
 */
public interface RecoderFactory {


    /**
     * 创建{@link Recoder}读对象
     * @param properties 配置数据
     * @return 读recoder list
     */
    <T> Map<String, Recoder<T>> readRecords(StorageProperties properties) throws SQLException, ClassNotFoundException, IOException;

    /**
     * 创建{@link Recoder}写对象
     * @param properties 配置数据
     * @return 写recoder list
     */
    <T> Map<String, Recoder<T>> writeRecords(StorageProperties properties) throws SQLException, ClassNotFoundException, IOException;

    /**
     * 每个实现类中，存储已经创建的recoder，因为存在着写、读的recoder。
     * 但是在对应实例过程中，写与读的recoder是可以进行recoder复用。
     * <p>复用存在以下规则才能进行复用：</p>
     * <p>配置相同与类型相同的可以进行复用，在实例化过程中，会把{@code screw.factories}文件下并且在配置文件中配置的所有的实例进行创建</p>
     * <p>key：{@link Recoder}名称</p>
     * <p>value：map存储，其中key为：entityClassName及某个实体的名称 ，value为对应实体</p>
     * @return 写读的recoder
     */
    <T> Map<String, Map<String, Recoder<T>>> getCache();

    /**
     * 向缓存中添加recoder
     * @param recoderName recoder的名称{@link Recoder}
     * @param recoder recoder实现类
     * @return {@link Map#putIfAbsent(Object, Object)}
     */
    default <T> Recoder<T> addCache(String recoderName, Recoder<T> recoder) {
        Map<String, Map<String, Recoder<T>>> cache = getCache();
        if (cache != null) {
            Map<String, Recoder<T>> entityToRecoder = cache.computeIfAbsent(recoderName, k -> new LinkedHashMap<>());
            String entityClassName = ClassUtils.getSingleGenericClassName(recoder.getClass());
            return entityToRecoder.putIfAbsent(entityClassName, recoder);
        }
        return null;
    }

    /**
     * 根据class对象创建recoder，如果当前recoder在{@link #getCache()}中存在，那么返回
     * @param className {@code screw.factories}定义下的所有的全限定类名
     * @param args recoder 构造参数列表
     * @return 如果在缓存中存在，返回缓存中的recoder，否则通过{@link Constructor}创建对象。如果在创建抛出异常，将返回null
     */
    default <T> Recoder<T> newRecoder(String className, Object... args) throws ClassNotFoundException {
        Class<Recoder<T>> clazz;
        try {
            clazz = (Class<Recoder<T>>) Class.forName(
                    className,
                    true,
                    Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new ClassNotFoundException(e.getMessage());
        }
        // 判断当前class是否是需要进行实例化
        Recoder.Callable callable = clazz.getAnnotation(Recoder.Callable.class);
        String name = callable.name();
        Recoder<T> recoder = null;
        // 从缓存中获取
        Map<String, Map<String, Recoder<Object>>> cache = getCache();
        StorageProperties properties = null;
        for (Object arg : args) {
            if (arg instanceof StorageProperties) {
                properties = (StorageProperties) arg;
            }
        }
        // 1.避免多实例创建，保证每个Recoder都只是单实例
        // 2.按照配置文件创建recoder
        if (properties != null) {
            // 从读、写recoder查找缓存中的数据
            Set<String> persistence = new HashSet<>();
            persistence.addAll(Arrays.asList(properties.getReadPersistence().split(StringPool.COMMA)));
            persistence.addAll(Arrays.asList(properties.getWritePersistence().split(StringPool.COMMA)));
            if (Collections.isEmpty(persistence)) {
                throw new NullPointerException("persistence type is empty");
            }
            if (!persistence.contains(name)) {
                return null;
            }
            for (String persistenceType : persistence) {
                Map<String, Recoder<Object>> entityRecoder = cache.get(persistenceType);
                if (entityRecoder == null) {
                    continue;
                }
                List<Recoder<Object>> collect = entityRecoder.values().stream()
                        .filter(o -> o.getClass().getName().equals(className))
                        .collect(Collectors.toList());
                if (Collections.isNotEmpty(collect)) {
                    recoder = (Recoder<T>) collect.get(0);
                }
            }
        }
        if (recoder == null) {
            recoder = (Recoder<T>) newRecord0(clazz, args);
        }
        if (recoder == null) {
            throw new NullPointerException(String.format("create failed %s", className));
        }
        return addCache(name, recoder);
    }

    /**
     * 具体创建recoder实例，由子类进行实现。目前默认采取cglib进行实现。
     * @param clazz 需要创建的recoder对象
     * @param args recoder 构造参数列表
     * @param <T> recode的泛型参数
     * @return 动态代理对象
     */
    <T> Recoder<T> newRecord0(Class<T> clazz, Object... args);

}
