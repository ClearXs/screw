package com.jw.screw.storage.hive.session;

import com.jw.screw.common.util.StringUtils;
import com.jw.screw.storage.hive.executor.Executor;
import com.jw.screw.storage.hive.executor.SimpleExecutor;
import com.jw.screw.storage.properties.HiveProperties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * 默认hiveQl session实现，没有考虑事务等问题
 * @author jiangw
 * @date 2021/7/2 16:42
 * @since 1.1
 */
class DefaultHqlSession implements HqlSession {

    private final HiveProperties hiveProperties;

    private Connection connection;

    private final Executor executor;

    DefaultHqlSession(HiveProperties hiveProperties) throws ClassNotFoundException, SQLException {
        this.hiveProperties = hiveProperties;
        String url = hiveProperties.getUrl();
        if (StringUtils.isEmpty(url)) {
            throw new NullPointerException("hive connect url is empty");
        }
        init();
        this.executor = new SimpleExecutor();
    }

    @Override
    public void init() throws ClassNotFoundException, SQLException {
        Class.forName(this.hiveProperties.getDriverClassName());
        // 创建连接
        this.connection = DriverManager.getConnection(this.hiveProperties.getUrl(),
                this.hiveProperties.getUser(),
                this.hiveProperties.getPassword());

        // 程序关闭时，JVM提供清理的钩子，以下几种情况视为程序关闭
        // 1.程序正常退出
        // 2.使用System.exit()
        // 3.终端使用Ctrl+C触发的中断
        // 4.系统关闭
        // 5.OutOfMemory宕机
        // 6.使用Kill pid命令干掉进程（注：在使用kill -9 pid时，是不会被调用的）
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
    }

    @Override
    public boolean createInnerTable(String tableName, Map<String, String> fields) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            return executor.createInnerTable(statement, tableName, fields);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public int loadDataByTextFile(String path, String tableName) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            return executor.loadDataByTextFile(statement, path, tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(statement);
        }
        return 0;
    }

    @Override
    public List<Map<String, Object>> query(String tableName) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            return executor.query(statement, tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(statement);
        }
        return null;
    }

    @Override
    public List<Map<String, Object>> query(String tableName, List<String> fields) {
        return null;
    }
}
