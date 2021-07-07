package com.jw.screw.monitor.opentracing;

import io.opentracing.Span;

/**
 * 当{@link Span#finish()}进行调用时，或把当前这个span存在在内存，es、或log output
 * @author jiangw
 * @date 2020/12/24 14:51
 * @since 1.0
 */
public interface Reporter {

    /**
     * finish时提交这个span
     * @see ScrewSpan
     */
    void report(ScrewSpan span);

    void close();
}
