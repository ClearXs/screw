package com.jw.screw.storage.hive.executor;

import com.jw.screw.common.constant.StringPool;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleExecutor implements Executor {

    @Override
    public boolean createInnerTable(Statement statement, String tableName, Map<String, String> fields) throws SQLException {
        String fieldAndType = "";
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            fieldAndType += entry.getKey() + " " + entry.getValue() + StringPool.COMMA;
        }
        fieldAndType = fieldAndType.substring(0, fieldAndType.lastIndexOf(StringPool.COMMA));
        String hql = "CREATE TABLE IF NOT EXISTS " +
                tableName +
                StringPool.LEFT_BRACKET +
                fieldAndType +
                StringPool.RIGHT_BRACKET +
                "ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE";
        return statement.execute(hql);
    }

    @Override
    public int loadDataByTextFile(Statement statement, String path, String tableName) throws SQLException {
        String hql = "LOAD DATA LOCAL INPATH" +
                StringPool.SINGLE_QUOTE +
                path +
                StringPool.SINGLE_QUOTE +
                " INTO TABLE " +
                tableName;
        return statement.executeUpdate(hql);
    }

    @Override
    public List<Map<String, Object>> query(Statement statement, String tableName) throws SQLException {
        StringBuilder hql = new StringBuilder();
        hql.append("SELECT * FROM ").append(tableName);
        ResultSet resultSet = null;
        List<Map<String, Object>> maps = new ArrayList<>();
        try {
            Map<String, Object> result = new HashMap<>();
            resultSet = statement.executeQuery(hql.toString());

            ResultSetMetaData metaData = resultSet.getMetaData();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> query(Statement statement, String tableName, List<String> fields) {
        return null;
    }
}
