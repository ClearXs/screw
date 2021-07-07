package com.jw.screw.monitor.opentracing;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.ThreadLocalScope;

/**
 * 参考自{@link ThreadLocalScope}，详细看{@link com.jw.screw.monitor.opentracing.ScrewScopeManager}
 * @author jiangw
 * @date 2020/12/25 9:41
 * @since 1.0
 */
public class ScrewScope implements Scope {

    private final ScrewSpan wrapper;

    private final boolean finishOnClose;


    public ScrewScope(ScrewSpan wrapper, boolean finishOnClose) {
        this.wrapper = wrapper;
        this.finishOnClose = finishOnClose;
    }

    @Override
    @Deprecated
    public void close() {
    }

    @Override
    public Span span() {
        return wrapper;
    }

    public boolean isFinishOnClose() {
        return finishOnClose;
    }
}
