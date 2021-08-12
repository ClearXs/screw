package com.jw.screw.calculate.spark;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

public class CalculatorTest {

    @Test
    public void testStatistic() throws InterruptedException {
        TaskCalculate taskCalculate = new TaskCalculate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 3);
        Date time = calendar.getTime();
        taskCalculate.calculateStatistic(time.getTime());
        Thread.currentThread().join();
    }

    @Test
    public void testSameServiceRangeTimeCount() {
        TaskCalculate taskCalculate = new TaskCalculate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 3);
        int count = taskCalculate.calculateSameServiceRangeTimeCount("screw-admin本地日志输出", calendar.getTimeInMillis(), (new Date()).getTime());
        System.out.println(count);
    }
}
