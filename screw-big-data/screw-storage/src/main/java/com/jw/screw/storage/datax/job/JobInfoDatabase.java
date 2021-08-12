package com.jw.screw.storage.datax.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jw.screw.common.util.Collections;
import com.jw.screw.storage.jdbc.JDBCHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JobInfoDatabase implements JobInfoStrategy {

    @Override
    public String execute(JobInfo jobInfo) {
        JSONObject connectionObj = JSONObject.parseObject(jobInfo.getConnection());
        if (!connectionObj.containsKey("jdbcUrl")) {
            return connectionObj.getString("value");
        }
        String username = connectionObj.getString("username");
        String password = connectionObj.getString("password");
        JSONArray jdbcUrls = connectionObj.getJSONArray("jdbcUrl");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < jdbcUrls.size(); i++) {
            String jdbcUrl = jdbcUrls.getString(i);
            try {
                Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                PreparedStatement preparedStatement = connection.prepareStatement(jobInfo.getValue());
                ResultSet set = preparedStatement.executeQuery();
                List<Map<String, Object>> results = JDBCHelper.toList(set);
                if (Collections.isNotEmpty(results)) {
                    for (Map<String, Object> result : results) {
                        for (Object value : result.values()) {
                            values.add(value.toString());
                        }
                    }

                }
                set.close();
                preparedStatement.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return String.join("", values);
    }
}
