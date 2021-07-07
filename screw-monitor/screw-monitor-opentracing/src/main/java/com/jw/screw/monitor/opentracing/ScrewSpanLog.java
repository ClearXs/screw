package com.jw.screw.monitor.opentracing;

import io.opentracing.log.Fields;

import java.util.Map;

/**
 * Span日志
 * @author jiangw
 * @date 2020/12/24 14:32
 * @since 1.0
 */
public class ScrewSpanLog {

    /**
     * record log time
     */
    private final long logTime;

    /**
     * @see Fields
     */
    private final Map<String, ?> fields;

    public ScrewSpanLog(long logTime, Map<String, ?> fields) {
        this.logTime = logTime;
        this.fields = fields;
    }

    public long getLogTime() {
        return logTime;
    }

    public Map<String, ?> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "ScrewSpanLog{" +
                "logTime=" + logTime +
                ", fields=" + fields +
                '}';
    }
}
