package com.jw.screw.storage;

import com.alibaba.fastjson.JSON;
import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.parser.FormatParser;
import com.jw.screw.common.util.Collections;
import com.jw.screw.common.util.FileUtils;
import com.jw.screw.storage.annotation.ReadWriteExecutor;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.Recoder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.*;
import java.util.List;
import java.util.Map;


/**
 * <b>提供存储层基本的插入、查询、更新等基本curd操作</b>
 * <p>读与写是可以进行分开的，设计的目的是基于读与写的入口和出口可能不同</p>
 * @author jiangw
 * @date 2021/7/7 16:08
 * @since 1.1
 */
public interface Executor {

    /**
     * <b>记录日志</b>
     * <p>如果是代理对象，子类不需要进行实现{@link com.jw.screw.storage.Executors}
     * @param entityClass 实体class对象
     * @param message 实体
     */
    @ReadWriteExecutor(write = true)
    default <T> void record(Class<T> entityClass, T message) {

    }

    /**
     * <b>获取消息实体</b>
     * <p>如果是代理对象，子类不需要进行实现{@link com.jw.screw.storage.Executors}
     * @param entityClass 实体class对象
     * @param id 消息实体id
     */
    @ReadWriteExecutor(read = true)
    default <T> T getMessage(Class<T> entityClass, String id) {
        return null;
    }

    /**
     * <b>获取所有消息实体</b>
     * <p>如果是代理对象，子类不需要进行实现{@link com.jw.screw.storage.Executors}
     * @param entityClass 实体class对象
     * @return 返回list数据
     */
    @ReadWriteExecutor(read = true)
    default <T> List<T> getAll(Class<T> entityClass) {
        return null;
    }

    /**
     * 查询过滤 {@link QueryFilter}
     * @param queryFilter
     * @return
     */
    @ReadWriteExecutor(read = true)
    default <T> List<T> query(Class<T> entityClass, QueryFilter<T> queryFilter) {
        return null;
    }

    /**
     * 关闭每个记录，可能是释放连接资源等
     */
    default void close() {
        Map<String, Recoder<Object>> readRecords = getReadRecords();
        if (Collections.isNotEmpty(readRecords)) {
            readRecords.values().forEach(Recoder::shutdownCallback);
        }
        Map<String, Recoder<Object>> writeRecords = getWriteRecords();
        if (Collections.isNotEmpty(writeRecords)) {
            writeRecords.values().forEach(Recoder::shutdownCallback);
        }
    }

    /**
     * 获取所有读recoder
     * @param <T> 实体
     * @return
     */
    <T> Map<String, Recoder<T>> getReadRecords();

    /**
     * 获取所有写recoder
     * @param <T> 实体
     * @return
     */
    <T> Map<String, Recoder<T>> getWriteRecords();

    /**
     * 获取recoder创建工厂
     * @return 在实现进行类返回这个工厂
     */
    com.jw.screw.storage.RecoderFactory getRecoderFactory();

    /**
     * 从类路径下获取配置文件，按照约定，可以直接配置screw-storage.yml，也可以从application.yml中进行配置
     * @return 如果获取得到则返回
     * @throws IOException 读取不到配置时抛出异常
     * @throws NullPointerException 当screw-storage.yml与application.yml都获取不到时抛出
     */
    default StorageProperties getStorageProperties() throws IOException {
        // 1.读取配置文件
        StorageProperties properties;
        try {
            byte[] bytes =  FileUtils.readFileByNIO("classpath:screw-storage.yml");
            String config = new String(bytes);
            String configJson = FormatParser.yamlToJson(config);
            properties = JSON.parseObject(configJson, StorageProperties.class);
        } catch (FileNotFoundException e) {
            try {
                // 尝试读取application.yml
                byte[] bytes =  FileUtils.readFileByNIO("classpath:application.yml");
                String config = new String(bytes);
                String propertiesStr = FormatParser.yamlToProperties(config);
                // 去除screw.storage的前缀
                StringBuilder screwLoggingProperties = new StringBuilder();
                for (String itemConfig : propertiesStr.split(StringPool.NEWLINE)) {
                    if (itemConfig.startsWith("screw.storage")) {
                        // + 1的目的是为了去除screw.storage.的.
                        String segment = itemConfig.substring("screw.storage".length() + 1);
                        screwLoggingProperties.append(segment).append(StringPool.NEWLINE);
                    }
                }
                String configJson = FormatParser.propertiesToJson(screwLoggingProperties.toString());
                properties = JSON.parseObject(configJson, StorageProperties.class);
            } catch (FileNotFoundException ex) {
                throw new NullPointerException(ex.getMessage());
            }
        }
        return properties;
    }

    /**
     * 表示当前Executor在不同创建条件（不同的构造方法）是可以进行共享，就是说只会创建一个Executor。
     * @see com.jw.screw.storage.ExecutorHousekeeper
     */
    @Deprecated
    @Inherited
    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Sharable {
    }
}
