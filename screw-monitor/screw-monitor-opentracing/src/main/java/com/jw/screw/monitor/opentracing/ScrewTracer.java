package com.jw.screw.monitor.opentracing;

import io.opentracing.*;
import io.opentracing.propagation.Format;

/**
 * 一个{@link Tracer}代表了一个rpc调用链路的执行过程，它是由{@link Span}组成的有向无环图，是一颗追踪树。
 * @author jiangw
 * @date 2020/12/24 14:02
 * @since 1.0
 */
public class ScrewTracer implements Tracer {

    /**
     * 一个ScopeManager能够获取当前线程启用的Span，并且可以启用一些处于未启用状态的span。
     * 但一个线程只能有一个span是启用的，其他的span要么等待子span，要么阻塞。
     */
    private final ScopeManager scopeManager;

    /**
     * span上报数据
     */
    private final Reporter reporter;

    /**
     * @see ScrewTracer#ScrewTracer(ScopeManager, Reporter)
     */
    public ScrewTracer() {
        this(new ScrewScopeManager(), new MemoryReporter());
    }

    /**
     * @see ScrewTracer#ScrewTracer(ScopeManager, Reporter)
     */
    public ScrewTracer(Reporter reporter) {
        this(new ScrewScopeManager(), reporter);
    }

    /**
     * 创建一个{@link Tracer}
     * <p>
     *     ScopeManager默认采用{@link ScrewScopeManager}实现。
     *     {@link ScrewScopeManager}采用{@link ThreadLocal}保存着{@link Scope}，如果第二次激活某个span，那么Scope对象将会改变
     *     在{@link ScrewScope#ScrewScope(ScrewSpan, boolean)}中改变
     * </p>
     * @param scopeManager {@link ScopeManager}
     * @param reporter {@link Reporter}
     */
    public ScrewTracer(ScopeManager scopeManager, Reporter reporter) {
        this.scopeManager = scopeManager;
        this.reporter = reporter;
    }

    @Override
    public ScopeManager scopeManager() {
        return scopeManager;
    }

    @Override
    public Span activeSpan() {
        Scope activeScope = scopeManager.active();
        if (activeScope != null) {
            return activeScope.span();
        } else {
            return null;
        }
    }

    @Override
    public ScrewSpanBuild buildSpan(String operationName) {
        return new ScrewSpanBuild(operationName)
                .scopeManager((ScrewScopeManager) scopeManager)
                .reporter(reporter);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {

    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return null;
    }
}
