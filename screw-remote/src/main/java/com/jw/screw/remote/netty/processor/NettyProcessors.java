package com.jw.screw.remote.netty.processor;

import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.model.Tuple;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * netty处理器的常量池
 * @author jiangw
 * @date 2020/11/29 18:25
 * @since 1.0
 */
public class NettyProcessors {

    private static final ExecutorService DEFAULT_EXECUTORS = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory("business"));

    public static ExecutorService defaultExec() {
        return DEFAULT_EXECUTORS;
    }

    public static Tuple<NettyProcessor, ExecutorService> simpleProcess() {
        return new Tuple<>(new SimpleNettyProcessor(), DEFAULT_EXECUTORS);
    }

    public static Tuple<NettyProcessor, ExecutorService> failProcess(Throwable cause) {
        return new Tuple<>(new NettyFastFailProcessor(cause), DEFAULT_EXECUTORS);
    }

}
