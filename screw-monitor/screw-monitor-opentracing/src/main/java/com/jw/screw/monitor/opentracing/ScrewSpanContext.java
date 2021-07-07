package com.jw.screw.monitor.opentracing;

import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 一个{@link SpanContext}表示一个{@link Span}的边界信息，如：
 * 1.spanId：  当前这个span的Id
 * 2.tracerId：这个Span所属的Tracer Id
 * 3.baggage： 能跨越多个调用单元的信息
 * @author jiangw
 * @date 2020/12/24 14:40
 * @since 1.0
 */
public class ScrewSpanContext implements SpanContext {

    /**
     * span id
     */
    private final String spanId;

    /**
     * span belong which one tracer id
     */
    private final String tracerId;

    /**
     * span context border propagation data
     */
    private final Map<String, String> baggage;

    public ScrewSpanContext(String tracerId, String spanId) {
        this(spanId, tracerId, new HashMap<>());
    }

    public ScrewSpanContext(String spanId, String tracerId, Map<String, String> baggage) {
        this.spanId = spanId;
        this.tracerId = tracerId;
        this.baggage = baggage;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return baggage.entrySet();
    }

    public String getSpanId() {
        return spanId;
    }

    public String getTracerId() {
        return tracerId;
    }

    public Map<String, String> getBaggage() {
        return baggage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScrewSpanContext that = (ScrewSpanContext) o;
        return Objects.equals(spanId, that.spanId) && Objects.equals(tracerId, that.tracerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spanId, tracerId);
    }

    @Override
    public String toString() {
        return "ScrewSpanContext{" +
                "spanId='" + spanId + '\'' +
                ", tracerId='" + tracerId + '\'' +
                ", baggage=" + baggage +
                '}';
    }
}
