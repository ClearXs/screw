package com.jw.screw.storage.properties;

import lombok.Data;

/**
 * orm使用mybatis
 * @author jiangw
 * @date 2021/7/21 17:04
 * @since 1.1
 */
@Data
public class DatabaseProperties {

    private String driverClassName;

    private int initialSize = 5;

    private int maxActive = 10;

    private int minIdle = 5;

    private String userName;

    private String password;

    private String url;
}
