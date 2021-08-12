package com.jw.screw.storage.properties;

import lombok.Data;

/**
 * redis，暂时只支持单机
 * @author jiangw
 * @date 2021/7/21 17:04
 * @since 1.1
 */
@Data
public class RedisProperties {

    private String host = "localhost";

    private int port = 6379;

    private int database = 0;

    private int timeout = 30000;
}
