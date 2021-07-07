package com.jw.screw.test.monitor.metrics;

import com.jw.screw.monitor.core.mircometer.Metrics;
import com.jw.screw.monitor.core.mircometer.MetricsObjectFactory;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsCollectionTest {

    @Test
    public void metricsCollection() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println();
                Map<String, List<Metrics>> metrics = MetricsObjectFactory.getMetrics();
                for (Map.Entry<String, List<Metrics>> entry : metrics.entrySet()) {
                    List<Metrics> value = entry.getValue();
                    for (Metrics metrics1 : value) {
                        System.out.println(metrics1);
                    }
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(2000);
    }
}
