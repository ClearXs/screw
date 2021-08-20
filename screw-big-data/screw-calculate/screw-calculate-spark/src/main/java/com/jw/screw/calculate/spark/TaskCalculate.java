package com.jw.screw.calculate.spark;

import com.jw.screw.calculate.spark.model.SourceStatistics;
import com.jw.screw.common.constant.StringPool;
import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.DefaultQueryFilter;
import com.jw.screw.storage.Executor;
import com.jw.screw.storage.ExecutorHousekeeper;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TaskCalculate {

    private final static String THREAD_NAME = "screw-calculate";

    private final AtomicInteger next = new AtomicInteger();

    private final ScheduledExecutorService calculator = new ScheduledThreadPoolExecutor(1, r -> {
        Thread thread = new Thread(r);
        thread.setName(THREAD_NAME + StringPool.UNDERSCORE + next.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    });

    private final Executor executor = ExecutorHousekeeper.getExecutor();

    private final LogTask logTask = new LogTask();

    private final AtomicLong lastExecuteTime = new AtomicLong();

    public TaskCalculate() {
    }

    protected long delayTime() {
        return 0;
    }

    public void start() {
        calculator.scheduleAtFixedRate(() -> {
            long currentTimeMillis = System.currentTimeMillis();
            calculateStatistic(lastExecuteTime.get());
            lastExecuteTime.set(currentTimeMillis);
        }, delayTime(), 100, TimeUnit.SECONDS);
    }

    public void stop() {
        calculator.shutdown();
    }

    /**
     * 统计来源
     */
    public void calculateStatistic(long lastExecuteTime) {
        long startTime = System.currentTimeMillis();
        Message message = new Message();
        if (lastExecuteTime > 0) {
            message.setCreateTime(new Date(lastExecuteTime));
        } else {
            message.setCreateTime(new Date());
        }
        DefaultQueryFilter<Message> messageDefaultQueryFilter = new DefaultQueryFilter<>(message);
        List<Message> messages = executor.query(Message.class, messageDefaultQueryFilter);
        List<SourceStatistics> statistics = logTask.statisticSource(messages);
        long endTime = System.currentTimeMillis();
        for (SourceStatistics statistic : statistics) {
            statistic.setStartTime(new Date(startTime));
            statistic.setEndTime(new Date(endTime));
            executor.record(SourceStatistics.class, statistic);
        }
    }

    public int calculateSameServiceRangeTimeCount(String service, long startTime, long endTime) {
        SourceStatistics sourceStatistics = new SourceStatistics();
        sourceStatistics.setSource(service);
        sourceStatistics.setStartTime(new Date(startTime));
        sourceStatistics.setEndTime(new Date(endTime));
        DefaultQueryFilter<SourceStatistics> queryFilter = new DefaultQueryFilter<>(sourceStatistics);
        List<SourceStatistics> statistics = executor.query(SourceStatistics.class, queryFilter);
        return logTask.statisticsSameServiceTimeRangeCount(statistics);
    }
}
