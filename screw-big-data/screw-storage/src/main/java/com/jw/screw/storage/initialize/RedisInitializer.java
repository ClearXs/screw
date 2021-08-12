package com.jw.screw.storage.initialize;

import com.jw.screw.storage.properties.RedisProperties;
import com.jw.screw.storage.properties.StorageProperties;
import redis.clients.jedis.Jedis;

class RedisInitializer implements RecordInitializer<Jedis> {

    @Override
    public Jedis init(StorageProperties properties, Object config) {
        RedisProperties redisProperties = properties.getRedis();
        return new Jedis(redisProperties.getHost(), redisProperties.getPort(), redisProperties.getTimeout());
    }
}
