package com.jw.screw.calculate.spark;


import com.jw.screw.calculate.spark.model.SourceStatistics;
import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.DefaultQueryFilter;
import com.jw.screw.storage.Executor;
import com.jw.screw.storage.ExecutorHousekeeper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SourceStatisticsTest {

    private static Executor executor;

    @BeforeAll
    public static void init() {
        executor = ExecutorHousekeeper.getExecutor();
    }

    @Test
    public void test() throws InterruptedException {
        Message message = new Message();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 1);
        Date time = calendar.getTime();
        message.setCreateTime(time);
        DefaultQueryFilter<Message> messageDefaultQueryFilter = new DefaultQueryFilter<>(message);
        List<Message> query = executor.query(Message.class, messageDefaultQueryFilter);
        LogTask logTask = new LogTask();
        List<SourceStatistics> statistics = logTask.statisticSource(query);
        for (SourceStatistics statistic : statistics) {
            executor.record(SourceStatistics.class, statistic);
        }
        Thread.sleep(10000);
    }
}
