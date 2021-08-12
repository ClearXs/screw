package com.jw.screw.logging.core.recoder;

import com.alibaba.fastjson.JSON;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.QueryFilter;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.AbstractRedisRecoder;

import java.util.List;
import java.util.stream.Collectors;

public class RedisMessageRecoder extends AbstractRedisRecoder<Message> {

    /**
     * {name:}创建namespace
     */
    private final static String KEY = "patrol:screw:logging.log";

    public RedisMessageRecoder(StorageProperties properties) {
        super(properties);
    }

    @Override
    protected Object getInitConfig() {
        return null;
    }

    @Override
    public void record(Message message) throws Exception {
        getJedis().hset(KEY, message.getId(), JSON.toJSONString(message));
    }

    @Override
    public Message getMessage(String id) {
        String message = getJedis().hget(KEY, id);
        if (StringUtils.isEmpty(message)) {
            return null;
        }
        return JSON.parseObject(message, Message.class);
    }

    @Override
    public List<Message> getAll() {
        List<String> messages = getJedis().hvals(KEY);
        return messages.stream()
                .map(message -> JSON.parseObject(message, Message.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> query(QueryFilter<Message> queryFilter) {
        return null;
    }
}
