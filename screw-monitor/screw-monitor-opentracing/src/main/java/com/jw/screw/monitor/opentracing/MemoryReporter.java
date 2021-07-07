package com.jw.screw.monitor.opentracing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * memory存储span
 * @author jiangw
 * @date 2020/12/24 14:55
 * @since 1.0
 */
public class MemoryReporter implements Reporter {

    private final List<ScrewSpan> spans = new CopyOnWriteArrayList<>();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void report(ScrewSpan span) {
        lock.readLock().lock();
        if (span != null) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                spans.add(span);
            } finally {
                lock.writeLock().unlock();
            }
        } else {
            lock.readLock().unlock();
        }
    }

    @Override
    public void close() {

    }

    public List<ScrewSpan> getSpans() {
        return spans;
    }
}
