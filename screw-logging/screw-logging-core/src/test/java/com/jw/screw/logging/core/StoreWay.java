package com.jw.screw.logging.core;

import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.DefaultQueryFilter;
import com.jw.screw.storage.Executor;
import com.jw.screw.storage.ExecutorHousekeeper;
import com.jw.screw.storage.QueryFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 测试日志记录器的存储方式
 * @author jiangw
 * @date 2021/7/8 14:15
 * @since 1.0
 */
public class StoreWay {

    private Executor executor;

    @Before
    public void init() throws SQLException, IOException, ClassNotFoundException {
        executor = ExecutorHousekeeper.getExecutor();
    }

    @Test
    public void testMemory() throws Exception {
        // screw-storage.yml persistence = memory
        executor.record(Message.class, getMessage());
    }

    @Test
    public void testFile() throws Exception {
        // screw-storage.yml persistence = file
        for (int i = 0; i < 101; i++) {
            executor.record(Message.class, getMessage());
        }
    }

    @Test
    public void testDb() throws Exception {
        // screw-storage.yml persistence = db
        for (int i = 0; i < 101; i++) {
            executor.record(Message.class, getMessage());
        }
    }

    @Test
    public void testRedis() throws Exception {
        // screw-storage.yml persistence = redis
        for (int i = 0; i < 101; i++) {
            executor.record(Message.class, getMessage());
        }
    }

    @Test
    public void testEs() throws Exception {
        // screw-storage.yml persistence = es
        for (int i = 0; i < 101; i++) {
            executor.record(Message.class, getMessage());
        }
    }

    @Test
    public void testHive() throws Exception {
        // screw-storage.yml persistence = hive
        for (int i = 0; i < 101; i++) {
            executor.record(Message.class, getMessage());
        }
    }

    @Test
    public void getAll() {
        List<Message> all = executor.getAll(Message.class);
        System.out.println(all);
    }

    @Test
    public void query() {
        Message message = new Message();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
        Date time = calendar.getTime();
        message.setCreateTime(time);
        QueryFilter<Message> queryFilter = new DefaultQueryFilter<>(message, 1, 10);
        List<Message> query = executor.query(Message.class, queryFilter);
        System.out.println(query);
    }

    private Message getMessage() {
        Message message = new Message();
        message.setSource("单元测试");
        return message;
    }

    @After
    public void close() throws InterruptedException {
        if (executor != null) {
            Thread.sleep(10000);
        }
    }
}
