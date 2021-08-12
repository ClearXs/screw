package com.jw.screw.storage;

import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.AbstractMemoryRecoder;

import java.io.IOException;
import java.util.List;

public class AbstractMemoryGenericRecoder extends AbstractMemoryRecoder<String> {

    protected AbstractMemoryGenericRecoder(StorageProperties properties) {
        super(properties);
    }

    @Override
    protected Object getInitConfig() {
        return null;
    }

    @Override
    protected void init(Object obj) throws IOException {

    }

    @Override
    public void record(String message) throws Exception {

    }

    @Override
    public String getMessage(String id) {
        return null;
    }

    @Override
    public List<String> getAll() {
        return null;
    }

    @Override
    public List<String> query(QueryFilter<String> queryFilter) {
        return null;
    }
}
