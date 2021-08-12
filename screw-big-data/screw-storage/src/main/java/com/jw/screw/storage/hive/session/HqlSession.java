package com.jw.screw.storage.hive.session;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public interface HqlSession {

    /**
     * 初始化hive连接
     */
    void init() throws ClassNotFoundException, SQLException;

    /**
     * 显示所有数据库信息
     * @return
     */
    default List<String> showDatabases() {
        return null;
    }

    /**
     * 创建数据库
     * @param databaseName
     * @return
     */
    default boolean createDatabase(String databaseName) {
        return false;
    }

    /**
     * 创建hive内部表
     * @param tableName 表名
     * @param fields 字段map集合，形如 id int, name string,
     * @return 是否成功 1:success 0:failure
     */
    boolean createInnerTable(String tableName, Map<String, String> fields);

    /**
     * 向本地文件系统加载数据到hive
     * @param path 文件路径地址，相对或绝对
     * @param tableName
     * @return 是否成功 1:success 0:failure
     */
    int loadDataByTextFile(String path, String tableName);

    /**
     * 查询hive数据
     * @param tableName 表名
     * @see #query(String, List)
     */
    List<Map<String, Object>> query(String tableName);

    /**
     * 查询hive数据
     * @param tableName
     * @param fields
     * @return
     */
    List<Map<String, Object>> query(String tableName, List<String> fields);

    /**
     * 关闭hive连接
     */
    default void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

}
