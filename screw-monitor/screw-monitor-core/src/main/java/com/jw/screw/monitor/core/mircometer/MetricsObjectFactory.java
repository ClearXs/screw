package com.jw.screw.monitor.core.mircometer;

import com.jw.screw.common.util.Collections;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.search.Search;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.io.File;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 性能指标的创建工厂
 * @author jiangw
 * @date 2020/12/22 15:05
 * @since 1.0
 */
public class MetricsObjectFactory {

    private final static ReentrantLock LOCK;

    /**
     * micrometer的指标注册器，维护最新的Meter
     */
    private final static SimpleMeterRegistry REGISTRY;

    /**
     * class loader-meter Key，包括：
     * jvm.classes.loaded -- 加载classes数
     * jvm.classes.unloaded -- 未加载的classes数
     */
    public final static String CLASS_LOADER_METRICS = "Class Loader Metrics";

    /**
     * disk space-meter Key，包括：
     * disk.total -- 当前目录的总大小
     * disk.free -- 当前目录可用的大小
     */
    public final static String DISK_SPACE_METRICS = "Disk Space Metrics";

    /**
     * gc-meter Key，包括：
     * jvm.gc.memory.promoted -- GC时，老年代分配的内存空间
     * jvm.gc.max.data.size -- GC时，老年代的最大内存空间
     * jvm.gc.memory.allocated -- GC时，年轻代分配的内存空间
     * jvm.gc.live.data.size -- 	FullGC时，老年代的内存空间
     */
    public final static String JVM_GC_METRICS = "Jvm Gc Metrics";

    /**
     * jvm memory-meter Key，包括：
     * jvm.memory.max -- JVM最大内存
     * jvm.memory.used	-- JVM已用内存
     * jvm.memory.committed -- JVM可用内存
     */
    public final static String JVM_MEMORY_METRICS = "Jvm Memory Metrics";

    /**
     * jvm thread-meter Key，包括：
     * jvm.threads.states -- JVM守护线程数
     * jvm.threads.live -- JVM当前活跃线程数
     * jvm.threads.peak -- JVM峰值线程数
     */
    public final static String JVM_THREAD_METRICS = "Jvm Thread Metrics";

    /**
     * process-meter Key 包括：
     * system.cpu.count -- CPU数量
     * system.cpu.usage -- 系统CPU使用率
     * process.cpu.usage -- 当前进程CPU使用率
     */
    public final static String PROCESSOR_METRICS = "Processor Metrics";

    /**
     * process time-meter Key 包括：
     * process.uptime -- 应用已运行时间
     * process.start.time -- 应用开启时间
     */
    public final static String UPTIME_METRICS = "UptimeMetrics";

    static {
        REGISTRY = new SimpleMeterRegistry();
        LOCK = new ReentrantLock();
    }

    public static Map<String, List<Metrics>> getMetrics() {
        Map<String, List<Metrics>> metrics = new HashMap<>();
        LOCK.lock();
        try {
            metrics.put(CLASS_LOADER_METRICS, getClassLoaderMetrics());
            metrics.put(DISK_SPACE_METRICS, getDiskSpaceMetrics());
            metrics.put(JVM_GC_METRICS, getJvmGcMetrics());
            metrics.put(JVM_MEMORY_METRICS, getJvmMemoryMetrics());
            metrics.put(JVM_THREAD_METRICS, getJvmThreadsMetrics());
            metrics.put(PROCESSOR_METRICS, getProcessorMetrics());
            metrics.put(UPTIME_METRICS, getUptimeMetrics());
        } finally {
            LOCK.unlock();
        }
        return metrics;
    }

    /**
     * @see MetricsObjectFactory#CLASS_LOADER_METRICS
     */
    private static List<Metrics> getClassLoaderMetrics() {
        new ClassLoaderMetrics().bindTo(REGISTRY);
        return build(CLASS_LOADER_METRICS);
    }

    /**
     * @see MetricsObjectFactory#DISK_SPACE_METRICS
     */
    private static List<Metrics> getDiskSpaceMetrics() {
        new DiskSpaceMetrics(new File(".")).bindTo(REGISTRY);
        return build(DISK_SPACE_METRICS);
    }

    private static List<Metrics> getJvmGcMetrics() {
        new JvmGcMetrics().bindTo(REGISTRY);
        return build(JVM_GC_METRICS);
    }

    private static List<Metrics> getJvmMemoryMetrics() {
        new JvmMemoryMetrics().bindTo(REGISTRY);
        return build(JVM_MEMORY_METRICS);
    }

    private static List<Metrics> getJvmThreadsMetrics() {
        new JvmThreadMetrics().bindTo(REGISTRY);
        return build(JVM_THREAD_METRICS);
    }

    private static List<Metrics> getProcessorMetrics() {
        new ProcessorMetrics().bindTo(REGISTRY);
        return build(PROCESSOR_METRICS);
    }

    private static List<Metrics> getUptimeMetrics() {
        new UptimeMetrics().bindTo(REGISTRY);
        return build(UPTIME_METRICS);
    }

    private static List<Metrics> build(String metricsKey) {
        List<Metrics> metricsList = new ArrayList<>();
        List<String> mappings = MetricsMapping.lookup(metricsKey);
        if (Collections.isNotEmpty(mappings)) {
            for (String mapping : mappings) {
                Collection<Meter> meters = REGISTRY.find(mapping).meters();
                for (Meter meter : meters) {
                    Metrics metrics = build(meter);
                    metricsList.add(metrics);
                }
            }
        }
        return metricsList;
    }

    private static Metrics build(Meter meter) {
        String name = meter.getId().getName();
        String baseUnit = meter.getId().getBaseUnit();
        String description = meter.getId().getDescription();
        Iterable<Measurement> measure = meter.measure();
        List<Metrics.Sample> measurements = new ArrayList<>();
        for (Measurement measurement : measure) {
            Metrics.Sample sample = new Metrics.Sample(measurement.getStatistic(), measurement.getValue());
            measurements.add(sample);
        }
        List<Tag> tags = meter.getId().getTags();
        return new Metrics(name, description, baseUnit, measurements, tags);
    }

    enum MetricsMapping {

        /**
         * @see MetricsObjectFactory#CLASS_LOADER_METRICS
         */
        CLASS_LOADER_METRICS(MetricsObjectFactory.CLASS_LOADER_METRICS,
                "jvm.classes.loaded", "jvm.classes.unloaded"),

        /**
         * @see MetricsObjectFactory#DISK_SPACE_METRICS
         */
        DISK_SPACE_METRICS(MetricsObjectFactory.DISK_SPACE_METRICS,
                "disk.total", "disk.free"),

        /**
         * @see MetricsObjectFactory#JVM_GC_METRICS
         */
        JVM_GC_METRICS(MetricsObjectFactory.JVM_GC_METRICS,
                "jvm.gc.memory.promoted", "jvm.gc.max.data.size", "jvm.gc.memory.allocated", "jvm.gc.live.data.size"),

        /**
         * @see MetricsObjectFactory#JVM_MEMORY_METRICS
         */
        JVM_MEMORY_METRICS(MetricsObjectFactory.JVM_MEMORY_METRICS,
                "jvm.memory.max", "jvm.memory.used", "jvm.memory.committed"),

        /**
         * @see MetricsObjectFactory#JVM_THREAD_METRICS
         */
        JVM_THREAD_METRICS(MetricsObjectFactory.JVM_THREAD_METRICS,
                "jvm.threads.states", "jvm.threads.live", "jvm.threads.peak"),

        /**
         * @see MetricsObjectFactory#PROCESSOR_METRICS
         */
        PROCESSOR_METRICS(MetricsObjectFactory.PROCESSOR_METRICS,
                "system.cpu.count", "system.cpu.usage", "process.cpu.usage"),

        /**
         * @see MetricsObjectFactory#UPTIME_METRICS
         */
        UPTIME_METRICS(MetricsObjectFactory.UPTIME_METRICS,
                "process.uptime", "process.start.time");

        /**
         * search Key
         */
        private final String key;

        /**
         * contains exact Names
         * @see Search#name(String)
         */
        private final List<String> mappingNames;

        MetricsMapping(String key, String... mappingNames) {
            this.key = key;
            this.mappingNames = Arrays.asList(mappingNames);
        }

        /**
         * 根据key找到需要的mapping
         */
        static List<String> lookup(String key) {
            for (MetricsMapping value : values()) {
                if (key.equals(value.key)) {
                    return value.mappingNames;
                }
            }
            return null;
        }
    }
}
