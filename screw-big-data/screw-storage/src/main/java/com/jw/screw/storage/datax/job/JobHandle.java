package com.jw.screw.storage.datax.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.util.Collections;
import com.jw.screw.common.util.FileUtils;
import com.jw.screw.common.util.IdUtils;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.storage.datax.properties.DataXProperties;
import com.jw.screw.storage.jdbc.JDBCHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class JobHandle {

    private static Logger logger = LoggerFactory.getLogger(JobHandle.class);

    private final JobInfoSelector selector = new JobInfoSelector();

    private DataXProperties properties;

    private static volatile JobHandle handle;

    private JobHandle() {

    }

    public void handle(DataXProperties properties) throws IOException, InterruptedException {
        this.properties = properties;
        for (String jobFile : properties.getJobFiles().split(StringPool.COMMA)) {
            // 1.读取文件
            byte[] bytes = FileUtils.readFileByNIO(jobFile);
            String newJson = parseJob(new String(bytes));
            // 2.读取配置项
            // 3.动态修改，创建临时文件
            String tempPath = generateTemJsonFile(newJson, properties.getDataxPath());
            String[] cmd = new String[]{"python", properties.getDataxPath() + "/datax.py", tempPath};
            // 4.执行命令
            final Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            StringBuilder lines = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                lines.append(line).append(StringPool.NEWLINE);
            }
            generateTemJsonFile(lines.toString(), properties.getDataxPath() + "\\logs\\");
            in.close();
            process.waitFor();
        }
    }

    private String parseJob(String oJob) {
        JSONObject newObj = JSONObject.parseObject(oJob);
        JSONObject job = newObj.getJSONObject("job");
        if (!newObj.containsKey("info")) {
            return oJob;
        }
        JSONObject info = newObj.getJSONObject("info");
        JSONArray content = job.getJSONArray("content");
        try {
            checkWriterIsValue(content);
            for (int i = 0; i < content.size(); i++) {
                JSONObject obj = content.getJSONObject(i);
                for (Map.Entry<String, Object> entry : obj.entrySet()) {
                    JSONObject entryValue = (JSONObject) entry.getValue();
                    JSONObject parameter = entryValue.getJSONObject("parameter");
                    replaceJobPlaceholder(content, parameter, info);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newObj.toString(SerializerFeature.PrettyFormat);
    }


    /**
     * 替换job中 reader或writer的占位符，如name=${name}
     * @param parameter reader或writer的parameter json串
     * @param info job info
     */
    private void replaceJobPlaceholder(JSONArray content, JSONObject parameter, JSONObject info) {
        for (Map.Entry<String, Object> entry : info.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            JobInfo jobInfo = JSON.parseObject(value.toString(), JobInfo.class);
            if (JobInfo.DATABASE.equals(jobInfo.getType())) {
                JSONObject connectionObj = getConnectionByJobJson(content, jobInfo.getReadBy());
                jobInfo.setConnection(connectionObj.toJSONString());
            }
            String selectValue = selector.select(jobInfo);
            // where替换
            if (parameter.containsKey("where")) {
                String where = parameter.getString("where");
                if (where.contains("${" + key + "}")) {
                    where = where.replace("${" + key + "}", selectValue);
                }
                parameter.put("where", where);
            }
            // querySql替换
            JSONArray connection = parameter.getJSONArray("connection");
            for (int i = 0; i < connection.size(); i++) {
                JSONObject connectionObj = connection.getJSONObject(i);
                if (connectionObj.containsKey("querySql")) {
                    JSONArray querySqls = connectionObj.getJSONArray("querySql");
                    for (int j = 0; j < querySqls.size(); j++) {
                        String querySql = querySqls.getString(j);
                        if (querySql.contains("${" + key + "}")) {
                            querySql = querySql.replace("${" + key + "}", selectValue);
                        }
                        querySqls.set(j, querySql);
                    }
                }
            }
        }
    }

    private JSONObject getConnectionByJobJson(JSONArray content, String readBy) {
        if (StringUtils.isEmpty(readBy)) {
            return new JSONObject();
        }
        JSONObject connectionObj = new JSONObject();
        for (int i = 0; i < content.size(); i++) {
            JSONObject contentObj = content.getJSONObject(i);
            for (Map.Entry<String, Object> entry : contentObj.entrySet()) {
                String key = entry.getKey();
                if (!readBy.equals(key)) {
                    continue;
                }
                JSONObject value = (JSONObject) entry.getValue();
                JSONObject parameter = value.getJSONObject("parameter");
                connectionObj.put("username", parameter.getString("username"));
                connectionObj.put("password", parameter.getString("password"));
                if (!connectionObj.containsKey("jdbcUrl")) {
                    connectionObj.put("jdbcUrl", new JSONArray());
                }
                JSONArray connection = (JSONArray) parameter.get("connection");
                for (int j = 0; j < connection.size(); j++) {
                    JSONObject connectionJSONObject = connection.getJSONObject(j);
                    JSONArray jdbcUrl = connectionObj.getJSONArray("jdbcUrl");
                    Object connectJdbcUrl = connectionJSONObject.get("jdbcUrl");
                    if (connectJdbcUrl instanceof JSONArray) {
                        jdbcUrl.addAll((JSONArray) connectJdbcUrl);
                    } else {
                        jdbcUrl.add(connectJdbcUrl);
                    }
                }
            }
        }
        return connectionObj;
    }

    /**
     * 检查writer是否有数据，如有writeMode则跟新为update，如果没有writeMode为insert，并删除reader的where过滤条件
     * @param content job content数据
     */
    private void checkWriterIsValue(JSONArray content) throws SQLException {
        boolean isFull = false;
        for (int i = 0; i < content.size(); i++) {
            JSONObject contentObj = content.getJSONObject(i);
            for (Map.Entry<String, Object> entry : contentObj.entrySet()) {
                String key = entry.getKey();
                if ("writer".equals(key)) {
                    JSONObject writerObj = (JSONObject) entry.getValue();
                    JSONObject parameter = writerObj.getJSONObject("parameter");
                    String username = parameter.getString("username");
                    String password = parameter.getString("password");
                    JSONArray connectArray = parameter.getJSONArray("connection");
                    for (int j = 0; j < connectArray.size(); j++) {
                        JSONObject connectObj = connectArray.getJSONObject(j);
                        String jdbcUrl = connectObj.getString("jdbcUrl");
                        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                        PreparedStatement preparedStatement = connection.prepareStatement(properties.getCheckSql().replace("${table}", connectObj.getJSONArray("table").getString(0)));
                        ResultSet set = preparedStatement.executeQuery();
                        List<Map<String, Object>> results = JDBCHelper.toList(set);
                        if (Collections.isNotEmpty(results)) {
                            isFull = true;
                            parameter.put("writeMode", "insert");
                        }
                        set.close();
                        preparedStatement.close();
                        connection.close();
                    }
                }
            }
            for (Map.Entry<String, Object> entry : contentObj.entrySet()) {
                String key = entry.getKey();
                if ("reader".equals(key)) {
                    JSONObject readerObj = (JSONObject) entry.getValue();
                    JSONObject parameter = readerObj.getJSONObject("parameter");
                    if (isFull) {
                        parameter.remove("where");
                    }
                }
            }
        }
    }

    private String generateTemJsonFile(String jobJson, String path) {
        String tmpFilePath;
        File file = new File(path);
        if (!file.isDirectory()) {
            file.mkdir();
        }
        tmpFilePath = path + "jobTmp-" + IdUtils.getNextIdAsString() + ".conf";
        // 根据json写入到临时本地文件
        try (PrintWriter writer = new PrintWriter(tmpFilePath, "UTF-8")) {
            writer.println(jobJson);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            logger.info("JSON 临时文件写入异常：" + e.getMessage());
        }
        return tmpFilePath;
    }

    public static JobHandle newInstance() {
        if (handle == null) {
            synchronized (JobHandle.class) {
                if (handle == null) {
                    handle = new JobHandle();
                }
            }
        }
        return handle;
    }
}
