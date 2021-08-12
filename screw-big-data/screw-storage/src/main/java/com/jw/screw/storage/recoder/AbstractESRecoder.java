package com.jw.screw.storage.recoder;

import com.jw.screw.storage.properties.StorageProperties;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

@Recoder.Callable(name = Recoder.ELASTICSEARCH)
public abstract class AbstractESRecoder<T> extends AbstractRecoder<T> {

    private RestHighLevelClient restHighLevelClient;

    protected AbstractESRecoder(StorageProperties properties) {
        super(properties);
    }

    @Override
    protected Object getInitConfig() {
        return null;
    }

    @Override
    protected void init(Object obj) throws IOException {
        if (obj instanceof RestHighLevelClient) {
            this.restHighLevelClient = (RestHighLevelClient) obj;
            initESIndex();
        }
    }

    public RestHighLevelClient getClient() {
        return this.restHighLevelClient;
    }

    /**
     * 初始化es索引
     */
    protected abstract void initESIndex() throws IOException;
}
