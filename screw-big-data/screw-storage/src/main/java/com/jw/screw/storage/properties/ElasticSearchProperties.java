package com.jw.screw.storage.properties;

import lombok.Data;

/**
 * es，暂时支持单机
 * @author jiangw
 * @date 2021/7/21 17:05
 * @since 1.1
 */
@Data
public class ElasticSearchProperties {

    /**
     * es index
     */
    private String index = "screw_storage_index";

    /**
     * es 主机
     */
    private String hostname = "localhost";

    /**
     * 端口
     */
    private int port = 9200;

    private String scheme = "http";
}
