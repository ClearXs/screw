package com.jw.screw.storage.hive.executor;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public interface Executor {

    /**
     * 创建hive内部表
     * @param tableName 表名
     * @param fields 字段map集合，形如 id int, name string,
     * @return 是否成功 1:success 0:failure
     */
    boolean createInnerTable(Statement statement, String tableName, Map<String, String> fields) throws SQLException;

    /**
     * 向本地文件系统加载数据到hive
     * @param path 文件路径地址，相对或绝对
     * @param tableName
     * @return 是否成功 1:success 0:failure
     */
    int loadDataByTextFile(Statement statement, String path, String tableName) throws SQLException;

    /**
     * 查询hive数据
     * @param tableName 表名
     * @see #query(Statement, String, List)
     */
    List<Map<String, Object>> query(Statement statement, String tableName) throws SQLException;

    /**
     * 查询hive数据
     * @param tableName
     * @param fields
     * @return
     */
    List<Map<String, Object>> query(Statement statement, String tableName, List<String> fields);
}
