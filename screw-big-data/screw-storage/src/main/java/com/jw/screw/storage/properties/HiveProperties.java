package com.jw.screw.storage.properties;

import lombok.Data;

/**
 * hive
 * @author jiangw
 * @date 2021/7/21 17:10
 * @since 1.1
 */
@Data
public class HiveProperties extends FileProperties {

    /**
     * 连接hive账号
     */
    private String user = "root";

    /**
     * hive密码
     */
    private String password = "";

    /**
     * jdbc:hive2://localhost:9999/{db_name}
     */
    private String url = "jdbc:hive2://localhost:9999/log_db";

    /**
     * hive的jdbc驱动
     */
    private String driverClassName = "org.apache.hive.jdbc.HiveDriver";

    /**
     * hive数据仓库表名
     */
    private String tableName = "logs";
}
