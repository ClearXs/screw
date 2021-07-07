package com.jw.screw.monitor.opentracing;

import com.jw.screw.common.util.IdUtils;
import io.opentracing.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @see Tracer.SpanBuilder
 * @author jiangw
 * @date 2020/12/25 15:20
 * @since 1.0
 */
public class ScrewSpanBuild implements Tracer.SpanBuilder {

    /**
     * span操作名称
     */
    private final String operationName;

    /**
     * span标签
     */
    private final Map<String, Object> tags;

    /**
     * span开启时间
     */
    private long startTime = 0;

    /**
     * 是否忽略当前active的span作为parent-span
     */
    private boolean ignoredActiveSpan = false;

    /**
     * @see ScrewSpanReferences
     */
    private List<ScrewSpanReferences> references = Collections.emptyList();

    /**
     * @see ScrewScopeManager
     */
    private ScrewScopeManager scopeManager;

    /**
     * @see Reporter
     */
    private Reporter reporter;

    ScrewSpanBuild(String operationName) {
        this.operationName = operationName;
        this.tags = new ConcurrentHashMap<>();
    }

    @Override
    public ScrewSpanBuild asChildOf(SpanContext parent) {
        return addReference(References.CHILD_OF, parent);
    }

    @Override
    public ScrewSpanBuild asChildOf(Span parent) {
        return asChildOf(parent.context());
    }

    @Override
    public ScrewSpanBuild addReference(String referenceType, SpanContext referencedContext) {
        if (referenceType == null || referenceType.toCharArray().length == 0) {
            return this;
        }
        if (referencedContext == null) {
            return this;
        }
        if (!(referencedContext instanceof ScrewSpanContext)) {
            return this;
        }
        ScrewSpanContext spanContext = (ScrewSpanContext) referencedContext;
        if (references.isEmpty()) {
            // 当前span只有一个父span（或）
            references = Collections.singletonList(new ScrewSpanReferences(referenceType, spanContext));
        } else {
            if (references.size() == 1) {
                references = new ArrayList<>(references);
            } else {
                references.add(new ScrewSpanReferences(referenceType, spanContext));
            }
        }
        return this;
    }

    @Override
    public ScrewSpanBuild ignoreActiveSpan() {
        this.ignoredActiveSpan = true;
        return this;
    }

    @Override
    public ScrewSpanBuild withTag(String key, String value) {
        this.tags.put(key, value);
        return this;
    }

    @Override
    public ScrewSpanBuild withTag(String key, boolean value) {
        this.tags.put(key, value);
        return this;
    }

    @Override
    public ScrewSpanBuild withTag(String key, Number value) {
        this.tags.put(key, value);
        return this;
    }

    public ScrewSpanBuild withTag(String key, List<String> value) {
        this.tags.put(key, value);
        return this;
    }

    public ScrewSpanBuild withTag(String key, Class<?> value) {
        if (value == null) {
            value = Object.class;
        }
        this.tags.put(key, value);
        return this;
    }

    public ScrewSpanBuild withTag(String key, long value) {
        this.tags.put(key, value);
        return this;
    }

    public ScrewSpanBuild withTag(String key, byte value) {
        this.tags.put(key, value);
        return this;
    }

    @Override
    public ScrewSpanBuild withStartTimestamp(long microseconds) {
        this.startTime = microseconds;
        return this;
    }

    public synchronized ScrewSpanBuild scopeManager(ScrewScopeManager scopeManager) {
        this.scopeManager = scopeManager;
        return this;
    }

    public synchronized ScrewSpanBuild reporter(Reporter reporter) {
        this.reporter = reporter;
        return this;
    }

    @Override
    public Scope startActive(boolean finishSpanOnClose) {
        return scopeManager.activate(start(), finishSpanOnClose);
    }

    @Override
    public Span startManual() {
        return null;
    }

    @Override
    public ScrewSpan start() {
        // 1.创建引用关系（如果是子的话）
        if (!ignoredActiveSpan && scopeManager.active() != null) {
            asChildOf(scopeManager.active().span());
        }
        // 2.创建上下文（TraceId、SpanId、baggage）
        ScrewSpanContext spanContext;
        if (references.isEmpty()) {
            spanContext = createParentContext();
        } else {
            spanContext = createChildContext();
        }
        // 3.创建开始时间
        if (startTime == 0L) {
            startTime = System.currentTimeMillis();
        }
        return new ScrewSpan(
                tags,
                new HashMap<>(),
                operationName,
                spanContext,
                references,
                startTime,
                reporter
        );
    }

    /**
     * 创建当前traceId
     * @return {@link ScrewSpanContext}
     */
    private ScrewSpanContext createParentContext() {
        String traceId = Long.toString(IdUtils.getNextId());
        return new ScrewSpanContext(traceId, Long.toString(IdUtils.getNextId()));
    }

    private ScrewSpanContext createChildContext() {
        // 寻找parentSpanContext
        ScrewSpanReferences exceptedReference = references.get(0);
        for (ScrewSpanReferences reference : references) {
            // !References.CHILD_OF.equals(exceptedReference.getType())可能存在FOLLOWS_FROM
            if (References.CHILD_OF.equals(reference.getType())
                    && !References.CHILD_OF.equals(exceptedReference.getType())) {
                exceptedReference = reference;
                break;
            }
        }
        ScrewSpanContext parentContext = exceptedReference.getSpanContext();
        // 子span构建父span的baggage，类似于继承的关系
        Map<String, String> baggage = null;
        for (ScrewSpanReferences reference : references) {
            Map<String, String> parentBaggage = reference.getSpanContext().getBaggage();
            if (parentBaggage != null) {
                if (baggage == null) {
                    baggage = new HashMap<>();
                }
                baggage.putAll(parentBaggage);
            }
        }
        return new ScrewSpanContext(
                Long.toString(IdUtils.getNextId()),
                parentContext.getTracerId(),
                baggage
        );
    }
}
