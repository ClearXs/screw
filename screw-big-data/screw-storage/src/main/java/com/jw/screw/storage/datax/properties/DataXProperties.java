package com.jw.screw.storage.datax.properties;

import lombok.Data;

/**
 * <b>dataX的配置属性</b>
 * @author jiangw
 * @date 2021/7/27 14:07
 * @since 1.1
 */
@Data
public class DataXProperties {

    /**
     * 是否开启
     */
    private boolean enable = false;

    /**
     * 任务模板文件
     */
    private String jobFiles = "";

    /**
     * dataX工具包路径
     */
    private String dataxPath = "";

    /**
     * 任务corn表达式，定时执行
     */
    private String jobCorn = "";


    /**
     * 用于检查dataX目标配置文件是否有数据，如果有数据则writeMode为update，否则为insert
     */
    private String checkSql = "";
}
