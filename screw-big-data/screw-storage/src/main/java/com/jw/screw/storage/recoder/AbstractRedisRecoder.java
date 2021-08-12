package com.jw.screw.storage.recoder;

import com.jw.screw.storage.properties.RedisProperties;
import com.jw.screw.storage.properties.StorageProperties;
import redis.clients.jedis.Jedis;

import java.io.IOException;

@Recoder.Callable(name = Recoder.REDIS)
public abstract class AbstractRedisRecoder<T> extends AbstractRecoder<T> {

    private Jedis jedis;

    protected AbstractRedisRecoder(StorageProperties properties) {
        super(properties);
    }

    @Override
    protected void init(Object obj) throws IOException {
        if (obj instanceof Jedis) {
            this.jedis = (Jedis) obj;
            StorageProperties properties = getProperties();
            RedisProperties redis = properties.getRedis();
            int database = redis.getDatabase();
            this.jedis.select(database);
        }
    }

    protected Jedis getJedis() {
        return this.jedis;
    }
}
