package com.jw.screw.common.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeoutException;

/**
 * @author jiangw
 * @date 2020/12/4 15:54
 * @since 1.0
 */
public interface InvokeFuture<V> extends RunnableFuture<V> {

    /**
     * future中存储的真实类型
     */
    Class<?> realClass();

    /**
     * 阻塞获取结果（默认30s），获取成功后通知那些监听的线程。
     */
    V getResult() throws ExecutionException, InterruptedException, TimeoutException;

    /**
     * 按照指定的事件获取结果
     * @param millis
     * @return
     */
    V getResult(long millis) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * 调用是否成功
     * @return
     */
    boolean isSuccess();

    /**
     * @return
     */
    Throwable getThrowable();

    /**
     * @param listener
     */
    InvokeFuture<V> addListener(FutureListener<V> listener);

    /**
     * 批量增加监听器
     * <p><b>弃用，可能产生堆污染。原因泛化数组转化时擦除成Object，将可能导致{@link ClassCastException}</b></p>
     * @param listeners
     */
    @Deprecated
    InvokeFuture<V> addListeners(FutureListener<V>... listeners);

    /**
     * @param listener
     */
    InvokeFuture<V> removeListener(FutureListener<V> listener);

    /**
     * 批量删除监听器。
     * <p><b>弃用，可能产生堆污染。原因泛化数组转化时擦除成Object，将可能导致{@link ClassCastException}</b></p>
     * @param listeners
     * @return
     */
    @Deprecated
    InvokeFuture<V> removeListeners(FutureListener<V>... listeners);

}
