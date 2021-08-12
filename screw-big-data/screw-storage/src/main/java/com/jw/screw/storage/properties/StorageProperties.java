package com.jw.screw.storage.properties;

import lombok.Data;

/**
 * <b>储存数据配置</b>
 * <p>包含的配置：</p>
 * <p>1.内容存储，采用{@link java.util.concurrent.ConcurrentHashMap}</p>
 * <p>2.数据库存储，使用mybatis作为ORM框架，使用时需要引入对应依赖包</p>
 * <p>3.redis存储，存储的结构为hash</p>
 * <p>4.es存储，在创建实例的时候建立所以与字段</p>
 * <p>5.文件存储</p>
 * <p>6.hive存储</p>
 * @author jiangw
 * @date 2021/7/2 9:47
 * @since 1.1
 */
@Data
public class StorageProperties {

    /**
     * 是否开启
     */
    private boolean enable = false;

    /**
     * 读持久化策略
     */
    private String readPersistence = "memory";

    /**
     * 写持久化策略
     */
    private String writePersistence = "memory";

    private MemoryProperties memory = new MemoryProperties();

    private DatabaseProperties database = new DatabaseProperties();

    private RedisProperties redis = new RedisProperties();

    private ElasticSearchProperties es = new ElasticSearchProperties();

    private FileProperties file = new FileProperties();

    private HiveProperties hive = new HiveProperties();

}
