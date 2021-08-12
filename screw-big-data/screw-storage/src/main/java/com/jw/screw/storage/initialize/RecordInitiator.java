package com.jw.screw.storage.initialize;

import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.Recoder;

/**
 * TDD
 * @author jiangw
 * @date 2021/7/22 17:42
 * @since 1.1
 */
public class RecordInitiator {

    /**
     * 维护一个initializer
     */
    private RecordInitializer<?> initializer;

    public Object apply(StorageProperties properties, Object config) {
        if (initializer != null) {
            return initializer.init(properties, config);
        }
        return null;
    }

    public void changeInitializer(String recorder) {
        if (Recoder.DATABASE.equals(recorder)) {
            initializer = new DatabaseInitializer();
        } else if (Recoder.ELASTICSEARCH.equals(recorder)) {
            initializer = new ESInitializer();
        } else if (Recoder.FILE.equals(recorder)) {
            initializer = new FileInitializer();
        } else if (Recoder.REDIS.equals(recorder)) {
            initializer = new RedisInitializer();
        } else if (Recoder.HIVE.equals(recorder)) {
            initializer = new HiveInitializer();
        } else if (Recoder.MEMORY.equals(recorder)) {
            initializer = new MemoryInitializer();
        }
    }

}
