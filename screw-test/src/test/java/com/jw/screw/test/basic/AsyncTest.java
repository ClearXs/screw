package com.jw.screw.test.basic;

import com.jw.screw.common.future.AbstractInvokeFuture;
import com.jw.screw.common.future.FutureListener;
import com.jw.screw.common.future.InvokeFuture;
import org.junit.Test;

import java.util.concurrent.*;

public class AsyncTest {

    @Test
    public void simple() throws InterruptedException, ExecutionException {
        SimpleInvoke<String> simpleInvoke = new SimpleInvoke<>("21");
        simpleInvoke.addListener(new FutureListener<String>() {
            @Override
            public void completed(String future, Throwable throwable) {
                System.out.println(future);
            }
        });

        System.out.println("21221");
        ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("22");
            }
        }, 1, 1, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(33);
    }

    static class SimpleInvoke<V> extends AbstractInvokeFuture<V> {

        public SimpleInvoke(V value) {
            super();
            this.callable = new SimpleCallable(value);
            taskExecutor.execute(this);
        }

        @Override
        public Class<?> realClass() {
            return null;
        }

        @Override
        public V getResult() throws ExecutionException, InterruptedException {
            return null;
        }

        @Override
        public V getResult(long millis) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }

        @Override
        public InvokeFuture<V> addListener(FutureListener<V> listener) {
            futureListeners.add(listener);
            return null;
        }

        @Override
        public final InvokeFuture<V> addListeners(FutureListener<V>... listeners) {
            return null;
        }

        @Override
        public InvokeFuture<V> removeListener(FutureListener<V> listener) {
            return null;
        }

        @Override
        public InvokeFuture<V> removeListeners(FutureListener<V>... listeners) {
            return null;
        }


        class SimpleCallable implements Callable<V> {

            private final V value;

            public SimpleCallable(V value) {
                this.value = value;
            }

            @Override
            public V call() throws Exception {
                Thread.sleep(3000);
                return value;
            }
        }
    }
}
