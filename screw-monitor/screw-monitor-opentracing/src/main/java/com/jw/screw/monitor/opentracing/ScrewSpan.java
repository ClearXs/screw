package com.jw.screw.monitor.opentracing;

import com.jw.screw.common.util.Collections;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;

import java.util.*;


/**
 * 链路追踪的节点数据，它代表rpc调用链中某一环的数据。
 * 一个span应该包含如下数据：
 * 1.operation name  操作名称
 * 2.start time      开始时间
 * 3.end-time        结束时间
 * 4.tags            标签，每个标签由key-value组成，标签是有利于调用分析的数据
 * 5.logs            日志，每个日志由key-value与log time组成，可以包含一系列错误堆栈与事件信息
 * 6.context         span上下文，主要用于span边界传递信息
 * 7.references      span之间的引用关系，关系又两种：父子or平行
 * @author jiangw
 * @date 2020/12/24 14:28
 * @since 1.0
 */
public class ScrewSpan implements Span {

    /**
     * @see Reporter
     */
    private transient final Reporter reporter;

    /**
     * @see Tags
     */
    private final Map<String, Object> tags;

    /**
     * @see SpanContext
     */
    private final Map<String, String> baggage;

    /**
     * 操作名称
     */
    private String operationName;

    /**
     * @see SpanContext
     */
    private final ScrewSpanContext context;

    /**
     * @see ScrewSpanReferences
     */
    private final List<ScrewSpanReferences> references;

    /**
     */
    private List<ScrewSpanLog> logs;

    /**
     * 开始时间
     */
    private final long startTime;

    /**
     * 结束时间
     */
    private long endTime;


    /**
     * 依据{@link References}引用链关系构建child
     */
    private transient Set<ScrewSpan> childSpans;

    /**
     * 兄弟span
     */
    private transient Set<ScrewSpan> siblingSpans;

    /**
     * 跟踪的span，用于那些异步执行的span
     */
    private transient Set<ScrewSpan> followSpans;

    public ScrewSpan(Map<String, Object> tags, Map<String, String> baggage,
                     String operationName, ScrewSpanContext context,
                     List<ScrewSpanReferences> references, long startTime, Reporter reporter) {
        this(tags, baggage, operationName, context, references, startTime, reporter, null);
    }

    public ScrewSpan(Map<String, Object> tags, Map<String, String> baggage,
                     String operationName, ScrewSpanContext context,
                     List<ScrewSpanReferences> references, long startTime, Reporter reporter, List<ScrewSpanLog> logs) {
        this.tags = tags;
        this.baggage = baggage;
        this.operationName = operationName;
        this.context = context;
        this.references = references;
        this.startTime = startTime;
        this.reporter = reporter;
        this.logs = logs;
    }

    @Override
    public ScrewSpanContext context() {
        return context;
    }

    @Override
    public synchronized ScrewSpan setTag(String key, String value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public synchronized ScrewSpan setTag(String key, boolean value) {
        tags.put(key, value);
        return this;
    }

    @Override
    public synchronized ScrewSpan setTag(String key, Number value) {
        tags.put(key, value);
        return this;
    }

    public synchronized ScrewSpan setTag(String key, List<Object> value) {
        this.tags.put(key, value);
        return this;
    }

    public synchronized ScrewSpan setTag(String key, Class<?> value) {
        if (value == null) {
            value = Object.class;
        }
        this.tags.put(key, value);
        return this;
    }

    public synchronized ScrewSpan setTag(String key, long value) {
        this.tags.put(key, value);
        return this;
    }

    public synchronized ScrewSpan setTag(String key, byte value) {
        this.tags.put(key, value);
        return this;
    }

    public synchronized ScrewSpan setTag(String key, Object value) {
        this.tags.put(key, value);
        return this;
    }

    @Override
    public synchronized Span log(Map<String, ?> fields) {
        return log(System.currentTimeMillis(), fields);
    }

    @Override
    public synchronized Span log(long timestampMicroseconds, Map<String, ?> fields) {
        if (this.logs == null) {
            this.logs = new ArrayList<>();
        }
        this.logs.add(new ScrewSpanLog(timestampMicroseconds, fields));
        return this;
    }

    @Override
    public synchronized Span log(String event) {
        return log(System.currentTimeMillis(), event);
    }

    @Override
    public synchronized Span log(long timestampMicroseconds, String event) {
        Map<String, String> fields = new HashMap<>();
        fields.put(Fields.EVENT, event);
        return log(timestampMicroseconds, fields);
    }

    public synchronized void childSpan(ScrewSpan childSpan) {
        if (Collections.isEmpty(childSpans)) {
            childSpans = new HashSet<>();
        }
        childSpans.add(childSpan);
    }

    public synchronized void siblingSpan(ScrewSpan siblingSpan) {
        if (Collections.isEmpty(siblingSpans)) {
            siblingSpans = new HashSet<>();
        }
        siblingSpans.add(siblingSpan);
    }

    public synchronized void followSpan(ScrewSpan followSpan) {
        if (Collections.isEmpty(followSpans)) {
            followSpans = new HashSet<>();
        }
        followSpans.add(followSpan);
    }

    @Override
    public synchronized Span setBaggageItem(String key, String value) {
        baggage.put(key, value);
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        return baggage.get(key);
    }

    @Override
    public synchronized Span setOperationName(String operationName) {
        this.operationName = operationName;
        return this;
    }

    public Object getTag(String tagName) {
        return tags.get(tagName);
    }

    public List<ScrewSpanReferences> getReferences() {
        return references;
    }

    @Override
    public void finish() {
        finish(System.currentTimeMillis());
    }

    @Override
    public void finish(long finishMicros) {
        endTime = finishMicros;
        if (reporter != null) {
            reporter.report(this);
        }
    }

    public Reporter getReporter() {
        return reporter;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public Map<String, String> getBaggage() {
        return baggage;
    }

    public String getOperationName() {
        return operationName;
    }

    public ScrewSpanContext getContext() {
        return context;
    }

    public List<ScrewSpanLog> getLogs() {
        return logs;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public Set<ScrewSpan> getChildSpans() {
        return childSpans;
    }

    public Set<ScrewSpan> getSiblingSpans() {
        return siblingSpans;
    }

    public Set<ScrewSpan> getFollowSpans() {
        return followSpans;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScrewSpan screwSpan = (ScrewSpan) o;
        return Objects.equals(context, screwSpan.context);
    }

    /**
     * 基于span context进行对象比较
     * @param context {@link ScrewSpanContext}
     * @return boolean
     */
    public boolean equals(ScrewSpanContext context) {
        if (this.context == context) {
            return true;
        }
        if (context == null) {
            return false;
        }
        return Objects.equals(this.context, context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context);
    }

    @Override
    public String toString() {
        return "ScrewSpan{" +
                "tags=" + tags +
                ", baggage=" + baggage +
                ", operationName='" + operationName + '\'' +
                ", context=" + context +
                ", references=" + references +
                ", logs=" + logs +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
