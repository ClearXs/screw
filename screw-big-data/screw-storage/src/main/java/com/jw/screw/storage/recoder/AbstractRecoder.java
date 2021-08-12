package com.jw.screw.storage.recoder;

import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.util.StringUtils;
import com.jw.screw.storage.initialize.RecordInitiator;
import com.jw.screw.storage.properties.StorageProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 提供日志记录的异步实现方式
 * @author jiangw
 * @date 2021/6/28 15:07
 * @since 1.1
 */
@Slf4j
public abstract class AbstractRecoder<T> implements Recoder<T> {

    private final String THREAD_NAME = "SCREW_RECODER";

    private final AtomicInteger next = new AtomicInteger();;

    /**
     * 记录日志线程池
     * 1.虚拟机可用的线程数据作为核心处理线程树
     * 2.1秒后销毁不用的线程
     * 3.拒绝策略采用抛出异常
     */
    private final ExecutorService recorderService;

    private final static RecordInitiator RECORD_INITIATOR = new RecordInitiator();

    /**
     * 消息记录锁
     */
    private final Lock lock = new ReentrantLock();

    private final StorageProperties properties;

    {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                recorderService.shutdownNow();
                shutdownCallback();
            }
        }));
    }

    /**
     * 子类需要实现的构造方法，每个子类进行自定义初始化，避免代码重复，提升代码抽象度
     * @param properties 配置文件
     */
    protected AbstractRecoder(StorageProperties properties) {
        this.properties = properties;
        Callable callable = this.getClass().getAnnotation(Callable.class);
        String name = callable.name();
        if (StringUtils.isEmpty(name)) {
            throw new NullPointerException("recoder name is empty, please check in contains Callable annotation");
        }
        RECORD_INITIATOR.changeInitializer(name);
        Object obj = RECORD_INITIATOR.apply(properties, getInitConfig());
        try {
            init(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorderService = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                (2 * Runtime.getRuntime().availableProcessors()) + 1,
                1000,
                TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName(THREAD_NAME + StringPool.UNDERSCORE + name + StringPool.UNDERSCORE + next.getAndIncrement());
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public Object execute(java.util.concurrent.Callable<Object> callable) {
        try {
            Future<?> submit = recorderService.submit(new java.util.concurrent.Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    lock.lock();
                    try {
                        if (callable != null) {
                            return callable.call();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                    return null;
                }
            });
            return submit.get();
        } catch (RejectedExecutionException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取初始化配置
     */
    protected abstract Object getInitConfig();

    /**
     * 对于记录失败的消息，提供模板方法
     * @param message 消息实体对象
     */
    protected void failureMessageHandle(T message) throws Exception {
        log.error("failure message: {}", message);
    }

    /**
     * 每一个{@link Recoder}初始化时进行调用
     * @param obj
     */
    protected abstract void init(Object obj) throws IOException;

    protected StorageProperties getProperties() {
        return this.properties;
    }
}
