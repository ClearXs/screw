package com.jw.screw.storage.initialize;

import com.jw.screw.storage.properties.MemoryProperties;
import com.jw.screw.storage.properties.StorageProperties;

import java.util.concurrent.ConcurrentHashMap;

class MemoryInitializer implements RecordInitializer<ConcurrentHashMap<String, Object>>{


    @Override
    public ConcurrentHashMap<String, Object> init(StorageProperties properties, Object config) {
        MemoryProperties memory = properties.getMemory();

        int capacity = memory.getCapacity();
        float loadFactor = memory.getLoadFactor();
        int concurrencyLevel = memory.getConcurrencyLevel();
        return new ConcurrentHashMap<String, Object>(capacity, loadFactor, concurrencyLevel);
    }
}
