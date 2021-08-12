package com.jw.screw.storage.datax.job;

import lombok.Data;

/**
 * <b>dataX配置文件中info属性</b>
 * <p>格式如下：</p>
 * <p>
 *     {
 * 	    "info": {
 * 		"modtime": {
 * 			"value": "select max(modtime) from gtws_net",
 * 			"type": "database",
 * 			"readBy": "writer"
*           }
 *     },
 *     "job": {
 *         "content": [
 *             {
 *                 "reader": {
 *                     "name": "sqlserverreader",
 *                     "parameter": {
 *                         "username": "sa",
 *                         "password": "123456",
 *                         "column": [
 *                             "*"
 *                         ],
 * 						"where": "modtime>'${modtime}'",
 *                 },
 *             }
 *         ]
 *     }
 * }
 * </p>
 * <p>
 *     info属性的作用是替换datax json文件中where过滤条件的占位符，提升程序灵活性
 * </p>
 * <p>
 *     实现过程中可以考虑采取三种方式：1.常量字符串、2.数据库查询结果、3.<a href="https://commons.apache.org/proper/commons-jexl/">jexl</a>表达计算结果
 *     采用策略模式进行设计，获取值
 * </p>
 * @author jiangw
 * @date 2021/7/27 14:38
 * @since 1.1
 */
@Data
public class JobInfo {

    public final static String DATABASE = "database";

    public final static String JEXL = "jexl";

    public final static String CONSTANT = "constant";

    /**
     * info类型，有三种：
     * 1.database
     * 2.jexl
     * 3.constant
     */
    private String type = CONSTANT;

    /**
     * 如果类型是database，那么才配置该熟悉
     */
    private String readBy;

    private String value;

    /**
     * 如果是database，连接json串
     */
    private String connection;
}
