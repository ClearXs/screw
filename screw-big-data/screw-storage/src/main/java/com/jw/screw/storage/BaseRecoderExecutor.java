package com.jw.screw.storage;

import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.Recoder;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author jiangw
 * @date 2021/6/28 14:56
 * @since 1.1
 */
public class BaseRecoderExecutor implements Executor {

    private Map<String, Recoder<Object>> readRecords;

    private Map<String, Recoder<Object>> writeRecords;

    private final StorageProperties storageProperties;

    public BaseRecoderExecutor() throws IOException, SQLException, ClassNotFoundException {
        this.storageProperties = getStorageProperties();
        init();
    }

    public BaseRecoderExecutor(StorageProperties properties) throws IOException, SQLException, ClassNotFoundException {
        this.storageProperties = properties;
        init();
    }

    protected void init() throws SQLException, IOException, ClassNotFoundException {
        RecoderFactory recoderFactory = getRecoderFactory();
        this.readRecords = recoderFactory.readRecords(this.storageProperties);
        this.writeRecords = recoderFactory.writeRecords(this.storageProperties);
    }

    @Override
    public RecoderFactory getRecoderFactory() {
        return new RecoderFactoryImpl();
    }

    @Override
    public StorageProperties getStorageProperties() throws IOException {
        if (this.storageProperties != null) {
            return this.storageProperties;
        }
        return Executor.super.getStorageProperties();
    }

    @Override
    public Map<String, Recoder<Object>> getReadRecords() {
        return this.readRecords;
    }

    @Override
    public Map<String, Recoder<Object>> getWriteRecords() {
        return this.writeRecords;
    }
}
