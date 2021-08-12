package com.jw.screw.logging.core.recoder;

import com.alibaba.fastjson.JSON;
import com.jw.screw.common.util.Collections;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.QueryFilter;
import com.jw.screw.storage.properties.StorageProperties;
import com.jw.screw.storage.recoder.AbstractESRecoder;
import org.apache.http.HttpStatus;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ESMessageRecoder extends AbstractESRecoder<Message> {

    private String index;

    public ESMessageRecoder(StorageProperties properties) {
        super(properties);
    }

    @Override
    protected void initESIndex() throws IOException {
        index = getProperties().getEs().getIndex();
        RestHighLevelClient client = getClient();
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);
        getIndexRequest.setTimeout(TimeValue.timeValueSeconds(1));
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (!exists) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
            Map<String, Map<String, Object>> properties = new HashMap<>();
            // 字段类型
            Map<String, Object> field = new HashMap<>();
            // id
            field.put("type", "text");
            properties.put("id", field);

            // create_time
            field = new HashMap<>();
            field.put("type", "Dates");
            properties.put("createTime", field);

            // source
            field = new HashMap<>();
            field.put("type", "text");
            properties.put("source", field);

            // type
            field = new HashMap<>();
            field.put("type", "text");
            properties.put("type", field);

            // content
            field = new HashMap<>();
            field.put("type", "text");
            properties.put("content", field);

            // host
            field = new HashMap<>();
            field.put("type", "text");
            properties.put("host", field);

            // trace_id
            field = new HashMap<>();
            field.put("type", "text");
            properties.put("traceId", field);

            Map<String, Object> mappings = new HashMap<>();
            mappings.put("properties", properties);
            createIndexRequest.mapping(mappings);
            client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        }
    }

    @Override
    public void record(Message message) throws Exception {
        IndexRequest request = new IndexRequest(index);
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
    public Message getMessage(String id) {
        if (StringUtils.isEmpty(id)) {
            throw new NullPointerException("id is empty");
        }
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(new MatchQueryBuilder("id", id));
        request.source(searchSourceBuilder);
        List<Message> messages;
        try {
            SearchResponse response = getClient().search(request, RequestOptions.DEFAULT);
            messages = Arrays.stream(response
                    .getHits().getHits())
                    .filter(hit -> {
                        Message message = JSON.parseObject(hit.getSourceAsString(), Message.class);
                        return id.equals(message.getId());
                    })
                    .map(hit -> JSON.parseObject(hit.getSourceAsString(), Message.class))
                    .collect(Collectors.toList());
            if (Collections.isEmpty(messages)) {
                return null;
            }
            return messages.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Message> getAll() {
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(new MatchAllQueryBuilder());
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        request.source(searchSourceBuilder);
        List<Message> messages = null;
        try {
            SearchResponse response = getClient().search(request, RequestOptions.DEFAULT);
            messages = Arrays.stream(response
                    .getHits().getHits())
                    .map(hit -> JSON.parseObject(hit.getSourceAsString(), Message.class))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public List<Message> query(QueryFilter<Message> queryFilter) {
        Message entity = queryFilter.getEntity();
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (StringUtils.isNotEmpty(entity.getSource())) {
            MatchQueryBuilder sourceMatchQuery = new MatchQueryBuilder("source", entity.getSource());
            boolQueryBuilder.must(sourceMatchQuery);
        }

        if (StringUtils.isNotEmpty(entity.getType())) {
            MatchQueryBuilder typeMatchQuery = new MatchQueryBuilder("type", entity.getType());
            boolQueryBuilder.must(typeMatchQuery);
        }

        if (StringUtils.isNotEmpty(entity.getContent())) {
            MatchQueryBuilder contentMatchQuery = new MatchQueryBuilder("content", entity.getContent());
            boolQueryBuilder.must(contentMatchQuery);
        }

        if (entity.getCreateTime() != null) {
            RangeQueryBuilder createTimeRangeQuery = new RangeQueryBuilder("createTime");
            createTimeRangeQuery.gte(entity.getCreateTime().getTime());
            boolQueryBuilder.must(createTimeRangeQuery);
        }
        searchSourceBuilder.query(boolQueryBuilder);

        searchSourceBuilder.from(queryFilter.getPage());
        searchSourceBuilder.size(queryFilter.getPageSize());
        searchRequest.source(searchSourceBuilder);

        List<Message> messages = null;
        try {
            SearchResponse response = getClient().search(searchRequest, RequestOptions.DEFAULT);
            messages = Arrays.stream(response
                    .getHits().getHits())
                    .map(hit -> JSON.parseObject(hit.getSourceAsString(), Message.class))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return messages;
    }
}
