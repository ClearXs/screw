package com.jw.screw.monitor.opentracing;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * tracer缓存，一个服务中只允许一个Tracer存在于内存中，用于解决调用链中无法获取tracer的问题。
 * <p>
 *     问题：在不同服务间，tracer通过数据传输可以获取到，但是存在当前服务在此发起rpc时，无法获取之前的tracer，如：
 * </p>
 * <p>
 *     serverA ----------->>>> serverB 此时可以通过request body获取tracer，但是再次调用serverC时无法传输tracer ------->>>> serverC
 * </p>
 *  <p>
 *      解决：一个server中只能存在一个tracer（即内存中），通过处理request时put，再次调用另外一个服务时take，发送response时clear
 *  </p>
 * @author jiangw
 * @date 2020/12/30 22:15
 * @since 1.0
 */
public class TracerCache {

    /**
     * 如果{@link #TRACER}不为空，那么则认为还存在有一个调用链路未完成，此时将进行阻塞，直至完成或超时。
     */
    private final static Lock LOCK = new ReentrantLock();

    /**
     * 放入的条件
     */
    private final static Condition PUT_CONDITION = LOCK.newCondition();

    /**
     * 等待时长
     */
    private final static long WAIT_MILLIS = 30000;

    /**
     * tracer cache
     */
    private final static AtomicReference<ScrewTracer> TRACER = new AtomicReference<>();

    /**
     * 把tracer放入缓存中
     * @param putTracer {@link ScrewTracer}
     */
    public static void put(ScrewTracer putTracer) {
        LOCK.lock();
        try {
            if (TRACER.get() == null) {
                TRACER.compareAndSet(null, putTracer);
            } else {
                if (PUT_CONDITION.awaitNanos(TimeUnit.MILLISECONDS.toNanos(WAIT_MILLIS)) <= 0) {
                    throw new NullPointerException();
                }
                TRACER.set(putTracer);
            }
        } catch (InterruptedException | NullPointerException e) {
            e.printStackTrace();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 获取当前调用链路的tracer
     * @return {@link ScrewTracer}
     */
    public static ScrewTracer take() {
        LOCK.lock();
        try {
            return TRACER.get();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 清除当前调用链路的tracer
     */
    public static void clear() {
        LOCK.lock();
        try {
            TRACER.set(null);
        } finally {
            LOCK.unlock();
        }
    }
}
