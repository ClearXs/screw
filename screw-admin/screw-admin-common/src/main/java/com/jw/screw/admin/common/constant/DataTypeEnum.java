/*
 * <<
 *  Davinci
 *  ==
 *  Copyright (C) 2016 - 2019 EDP
 *  ==
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  >>
 *
 */

package com.jw.screw.admin.common.constant;

import lombok.extern.slf4j.Slf4j;

/**
 * screw
 * @author jiangw
 * @date 2020/12/20 21:07
 * @since 1.0
 */
@Slf4j
public enum DataTypeEnum {

    /**
     * mysql
     */
    MYSQL("mysql", "mysql", "com.mysql.jdbc.Driver","jdbc:mysql://{ip}:{port}/{dbname}"),

    /**
     * oracle
     */
    ORACLE("oracle", "oracle", "oracle.jdbc.driver.OracleDriver","jdbc:oracle:thin:@{ip}:{port}:{dbname}"),

    /**
     * sql server
     */
    SQLSERVER("sqlserver", "sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver","jdbc:sqlserver://{ip}:{port};DatabaseName={dbname}");

    private String feature;
    private String desc;
    private String driver;
    private String jdbcUrl;


    private static final String JDBC_URL_PREFIX = "jdbc:";

    DataTypeEnum(String feature, String desc, String driver,String jdbcUrl) {
        this.feature = feature;
        this.desc = desc;
        this.driver = driver;
        this.jdbcUrl = jdbcUrl;
    }

    public static DataTypeEnum urlOf(String jdbcUrl) {
        String url = jdbcUrl.toLowerCase().trim();
        for (DataTypeEnum dataTypeEnum : values()) {
            if (url.startsWith(JDBC_URL_PREFIX + dataTypeEnum.feature)) {
                try {
                    Class<?> aClass = Class.forName(dataTypeEnum.getDriver());
                    if (null == aClass) {
                        throw new RuntimeException("Unable to get driver instance for jdbcUrl: " + jdbcUrl);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Unable to get driver instance: " + jdbcUrl);
                }
                return dataTypeEnum;
            }
        }
        return null;
    }

    public static DataTypeEnum typeOf(String feature) {
        for (DataTypeEnum dataTypeEnum : values()) {
            if (dataTypeEnum.feature.equalsIgnoreCase(feature)) {
                try {
                    Class<?> aClass = Class.forName(dataTypeEnum.getDriver());
                    if (null == aClass) {
                        throw new RuntimeException("Unable to get driver instance for type: " + feature);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Unable to get driver instance: " + feature);
                }
                return dataTypeEnum;
            }
        }
        return null;
    }

    public String getFeature() {
        return feature;
    }

    public String getDesc() {
        return desc;
    }

    public String getDriver() {
        return driver;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
}
