package com.jw.screw.common.future;


import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.util.Collections;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 参考{@link FutureTask}实现
 * @author jiangw
 */
public abstract class AbstractInvokeFuture<V> implements InvokeFuture<V> {

    private final AtomicInteger state = new AtomicInteger();

    private final static int NEW = 0;
    private final static int COMPLETED = 1;
    private final static int THROWABLE = 2;
    private final static int INTERRUPTED = 3;

    private V result;

    private Throwable throwable;

    protected final List<FutureListener<V>> futureListeners = new CopyOnWriteArrayList<>();

    protected final ExecutorService taskExecutor = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(), new NamedThreadFactory("async invoker"));

    protected final ReentrantLock lock = new ReentrantLock();

    protected final Condition wait = lock.newCondition();

    protected Callable<V> callable;

    public AbstractInvokeFuture() {
        state.set(NEW);
    }

    @Override
    public boolean isDone() {
        return taskExecutor.isShutdown();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        lock.lock();
        try {
            if (result != null) {
                return result;
            }
            boolean await = wait.await(30000, TimeUnit.MILLISECONDS);
            if (await) {
                return result;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        lock.lock();
        try {
            if (result != null) {
                return result;
            }
            boolean waitFor = wait.await(timeout, unit);
            if (waitFor) {
                return result;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        ReentrantLock lock = lock();
        lock.lock();
        try {
            V call = callable.call();
            state.set(COMPLETED);
            setResult(call);
        } catch (Exception e) {
            state.set(THROWABLE);
            throwable = e;
        } finally {
            wait.signalAll();
            lock.unlock();
            try {
                notifyListeners();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!(state.get() == NEW &&
                state.compareAndSet(NEW, INTERRUPTED))) {
            return false;
        }
        if (taskExecutor.isTerminated()) {
            return true;
        }
        if (mayInterruptIfRunning) {
            taskExecutor.shutdownNow();
        }
        return true;
    }

    @Override
    public boolean isSuccess() {
        return state.get() == COMPLETED;
    }

    @Override
    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public InvokeFuture<V> addListener(FutureListener<V> listener) {
        futureListeners.add(listener);
        return this;
    }

    @Override
    public InvokeFuture<V> addListeners(FutureListener<V>... listeners) {
        return this;
    }

    @Override
    public InvokeFuture<V> removeListener(FutureListener<V> listener) {
        futureListeners.remove(listener);
        return this;
    }

    @Override
    public InvokeFuture<V> removeListeners(FutureListener<V>... listeners) {
        return this;
    }


    @Override
    protected void finalize() throws Throwable {
        taskExecutor.shutdownNow();
    }

    @Override
    public boolean isCancelled() {
        return state.get() == INTERRUPTED;
    }

    public ReentrantLock lock() {
        return lock;
    }

    private void setResult(V result) {
        this.result = result;
    }

    private void notifyListeners() throws Exception {
        if (Collections.isNotEmpty(futureListeners)) {
            for (FutureListener<V> listener : futureListeners) {
                listener.completed(result, throwable);
            }
        }
    }
}
