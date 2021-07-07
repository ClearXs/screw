package com.jw.screw.monitor.opentracing;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.util.ThreadLocalScopeManager;

import java.util.Stack;

/**
 * 参考自{@link ThreadLocalScopeManager}。存储的Scope由ThreadLocal改为Stack，当获取active时，返回栈顶的元素。
 * 只有{@link Scope#close()}时才释放栈顶元素
 *      active scope                active scope
 *             scope    -> close()         scope
 *             scope
 * @author jiangw
 * @date 2020/12/24 22:28
 * @since 1.0
 */
public class ScrewScopeManager implements ScopeManager {

    final Stack<ScrewScope> screwScopes = new Stack<>();

    @Override
    public Scope activate(Span span, boolean finishSpanOnClose) {
        ScrewScope screwScope = new ScrewScope((ScrewSpan) span, finishSpanOnClose);
        screwScopes.push(screwScope);
        return screwScope;
    }

    /**
     * 关闭激活的scope
     */
    public void close() {
        if (screwScopes.isEmpty()) {
            return;
        }

        ScrewScope activeScope = screwScopes.peek();

        if (screwScopes.peek() != activeScope) {
            return;
        }

        if (activeScope.isFinishOnClose()) {
            activeScope.span().finish();
        }

        // 释放当前激活的scope
        screwScopes.pop();
    }

    @Override
    public Scope active() {
        if (screwScopes.isEmpty()) {
            return null;
        }
        return screwScopes.peek();
    }
}
