package com.jw.screw.storage.initialize;

import com.jw.screw.storage.properties.ElasticSearchProperties;
import com.jw.screw.storage.properties.StorageProperties;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

class ESInitializer implements RecordInitializer<RestHighLevelClient> {

    @Override
    public RestHighLevelClient init(StorageProperties properties, Object config) {
        ElasticSearchProperties esProperties = properties.getEs();
        return new RestHighLevelClient(RestClient.builder(
                new HttpHost(esProperties.getHostname(), esProperties.getPort(), esProperties.getScheme()))
        );
    }
}
