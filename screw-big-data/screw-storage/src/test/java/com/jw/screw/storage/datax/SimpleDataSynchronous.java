package com.jw.screw.storage.datax;

import org.junit.Test;
import org.quartz.SchedulerException;

public class SimpleDataSynchronous {

    @Test
    public void test() throws SchedulerException, InterruptedException {
        JobTask jobTask = new JobTask();

        Thread.currentThread().join();
    }
}
