package com.jw.screw.registry;

import com.jw.screw.common.NamedThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RegisterStater {

    public static void main(String[] args) throws InterruptedException {
        Registry registry = new DefaultRegistry(8501);
        ExecutorService registryExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                new NamedThreadFactory("screw registry"));
        registryExecutor.submit(new Runnable() {
            @Override
            public void run() {
                registry.start();
            }
        });
        Thread.currentThread().join();
    }
}
