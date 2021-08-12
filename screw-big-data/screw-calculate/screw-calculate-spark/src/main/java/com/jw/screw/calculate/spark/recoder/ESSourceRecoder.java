package com.jw.screw.calculate.spark.recoder;

import com.alibaba.fastjson.JSON;
import com.jw.screw.calculate.spark.model.SourceStatistics;
import com.jw.screw.storage.QueryFilter;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.AbstractESRecoder;
import org.apache.http.HttpStatus;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ESSourceRecoder extends AbstractESRecoder<SourceStatistics> {

    private final static String INDEX =  "source-statistics";

    public ESSourceRecoder(StorageProperties properties) {
        super(properties);
    }

    @Override
    protected void initESIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(INDEX);
        getIndexRequest.setTimeout(TimeValue.timeValueSeconds(1));
        boolean exists = getClient().indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (!exists) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(INDEX);
            Map<String, Map<String, Object>> properties = new HashMap<>();
            Map<String, Object> field = new HashMap<>();

            // id
            field.put("type", "text");
            properties.put("id", field);

            // source
            field = new HashMap<>();
            field.put("type", "text");
            properties.put("source", field);

            // count
            field = new HashMap<>();
            field.put("type", "integer");
            properties.put("count", field);

            // statistic time
            field = new HashMap<>();
            field.put("type", "date");
            properties.put("statisticTime", field);

            // start time
            field = new HashMap<>();
            field.put("type", "date");
            properties.put("startTime", field);

            // end time
            field = new HashMap<>();
            field.put("type", "date");
            properties.put("endTime", field);
            Map<String, Object> mappings = new HashMap<>();
            mappings.put("properties", properties);
            createIndexRequest.mapping(mappings);
            getClient().indices().create(createIndexRequest, RequestOptions.DEFAULT);
        }
    }


    @Override
    public void record(SourceStatistics message) throws Exception {
        IndexRequest request = new IndexRequest(INDEX);
        IndexResponse response = getClient().index(request
                        .timeout(TimeValue.timeValueSeconds(1))
                        .id(message.getId())
                        .source(JSON.toJSONString(message), XContentType.JSON)
                , RequestOptions.DEFAULT);
        RestStatus status = response.status();
        int resultStatus = status.getStatus();
        if (resultStatus != HttpStatus.SC_CREATED) {
            throw new NullPointerException(status.name());
        }
    }

    @Override
    public SourceStatistics getMessage(String id) {
        return null;
    }

    @Override
    public List<SourceStatistics> getAll() {
        return null;
    }

    @Override
    public List<SourceStatistics> query(QueryFilter<SourceStatistics> queryFilter) {
        return null;
    }
}
