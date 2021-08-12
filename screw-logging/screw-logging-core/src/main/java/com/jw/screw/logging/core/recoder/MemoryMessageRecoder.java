package com.jw.screw.logging.core.recoder;

import com.jw.screw.logging.core.constant.TransferType;
import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.QueryFilter;
import com.jw.screw.storage.properties.MemoryProperties;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.AbstractMemoryRecoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存日志记录器，器内存默认采用{@link ConcurrentHashMap}，它的配置为{@link MemoryProperties}
 * @author jiangw
 * @date 2021/6/28 16:47
 * @since 1.1
 */
public class MemoryMessageRecoder extends AbstractMemoryRecoder<Message> {

    private final Map<String, Message> messages;

    public MemoryMessageRecoder(StorageProperties properties) {
        super(properties);
        MemoryProperties memoryProperties = properties.getMemory();
        int capacity = memoryProperties.getCapacity();
        float loadFactor = memoryProperties.getLoadFactor();
        int concurrencyLevel = memoryProperties.getConcurrencyLevel();
        this.messages = new ConcurrentHashMap<>(capacity, loadFactor, concurrencyLevel);
    }

    @Override
    protected void init(Object obj) throws IOException {

    }

    @Override
    public void record(Message message) throws Exception {
        message.setTransferType(TransferType.LOCAL);
        messages.put(message.getId(), message);
    }

    @Override
    public Message getMessage(String id) {
        return messages.get(id);
    }

    @Override
    public List<Message> getAll() {
        return new ArrayList<>(messages.values());
    }

    @Override
    public List<Message> query(QueryFilter<Message> queryFilter) {
        return null;
    }

    @Override
    protected Object getInitConfig() {
        return null;
    }
}
