package com.jw.screw.monitor.opentracing;

import io.opentracing.References;

/**
 * 表示不同span之间的引用关系
 * @author jiangw
 * @date 2020/12/24 14:48
 * @since 1.0
 */
public class ScrewSpanReferences {

    /**
     * @see References
     */
    private final String type;

    /**
     * @see ScrewSpanContext
     */
    private final ScrewSpanContext spanContext;

    public ScrewSpanReferences(String type, ScrewSpanContext spanContext) {
        this.type = type;
        this.spanContext = spanContext;
    }

    public String getType() {
        return type;
    }

    public ScrewSpanContext getSpanContext() {
        return spanContext;
    }
}
