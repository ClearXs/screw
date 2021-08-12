package com.jw.screw.storage.jdbc;

import lombok.Data;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一些jdbc帮助方法，{@link ResultSet}转换为{@link Map}
 * @author jiangw
 * @date 2021/7/27 16:14
 * @since 1.1
 */
public class JDBCHelper {

    public static List<Map<String, Object>> toList(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<Column> columns = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            Column column = new Column();
            column.setIndex(i);
            column.setColumnLabel(metaData.getColumnLabel(i));
            column.setColumnName(metaData.getColumnName(i));
            column.setColumnType(metaData.getColumnType(i));
            column.setColumnTypeName(metaData.getColumnTypeName(i));
            column.setColumnDisplaySize(metaData.getColumnDisplaySize(i));
            column.setCatalogName(metaData.getCatalogName(i));
            column.setPrecision(metaData.getPrecision(i));
            column.setTableName(metaData.getTableName(i));
            column.setSchemaName(metaData.getSchemaName(i));
            columns.add(column);
        }
        List<Map<String, Object>> results = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> result = new HashMap<>();
            for (Column column : columns) {
                result.put(column.getColumnName(), resultSet.getObject(column.getIndex()));
            }
            results.add(result);
        }
        return results;
    }

    @Data
    static class Column {

        private int index;

        /**
         * @see ResultSetMetaData#getColumnName(int)
         */
        private String columnName;

        /**
         * @see ResultSetMetaData#getColumnLabel(int)
         */
        private String columnLabel;

        /**
         * @see ResultSetMetaData#getColumnDisplaySize(int)
         */
        private int columnDisplaySize;

        /**
         * @see ResultSetMetaData#getSchemaName(int)
         */
        private String schemaName;

        /**
         * @see ResultSetMetaData#getPrecision(int)
         */
        private int precision;

        /**
         * @see ResultSetMetaData#getTableName(int)
         */
        private String tableName;

        /**
         * @see ResultSetMetaData#getCatalogName(int)
         */
        private String catalogName;

        /**
         * @see ResultSetMetaData#getColumnType(int)
         */
        private int columnType;

        /**
         * @see ResultSetMetaData#getColumnTypeName(int)
         */
        private String columnTypeName;
    }
}
