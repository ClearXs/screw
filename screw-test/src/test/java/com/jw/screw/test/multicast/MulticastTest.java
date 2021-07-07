package com.jw.screw.test.multicast;

import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.exception.InvokeFutureException;
import com.jw.screw.common.future.FutureListener;
import com.jw.screw.common.future.InvokeFuture;
import com.jw.screw.common.future.InvokeFutureContext;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.consumer.ConnectionWatcher;
import com.jw.screw.consumer.Listeners;
import com.jw.screw.consumer.NettyConsumer;
import com.jw.screw.consumer.RepeatableFuture;
import com.jw.screw.consumer.model.ProxyObjectFactory;
import com.jw.screw.provider.NettyProvider;
import com.jw.screw.provider.NettyProviderConfig;
import com.jw.screw.provider.Notifier;
import com.jw.screw.provider.annotations.ProviderService;
import com.jw.screw.registry.DefaultRegistry;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MulticastTest {

    @Test
    public void register() {
        DefaultRegistry defaultRegistry = new DefaultRegistry(8080);
        defaultRegistry.start();
    }

    @Test
    public void provider1() throws InterruptedException, NoSuchMethodException, ConnectionException {
        NettyProviderConfig providerConfig = new NettyProviderConfig();
        providerConfig.setServerKey("demo");
        providerConfig.setWeight(4);
        providerConfig.setPort(8082);
        DemoServiceImpl demoService = new DemoServiceImpl();
        final NettyProvider nettyProvider = new NettyProvider(providerConfig);
        nettyProvider.publishServices(demoService);
        nettyProvider.registry(new RemoteAddress("localhost", 8080));
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    nettyProvider.start();
                } catch (InterruptedException | ConnectionException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        TimeUnit.SECONDS.sleep(3);

        Notifier notifier = new Notifier(nettyProvider);
        String hello = demoService.hello("21");
        notifier.unicast(hello, DemoServiceImpl.class, "hello", new Class<?>[] {String.class});

        TimeUnit.SECONDS.sleep(3);
        String msg = demoService.msg();
        notifier.unicast(msg, DemoServiceImpl.class, "msg", new Class<?>[] {});
        TimeUnit.SECONDS.sleep(2000);
        nettyProvider.shutdown();
    }

    @Test
    public void consumer() throws ConnectionException, InterruptedException, ExecutionException, InvokeFutureException {
        NettyConsumer nettyConsumer = new NettyConsumer();
        ServiceMetadata metadata = new ServiceMetadata("demo");
        nettyConsumer.register("localhost", 8080);
        nettyConsumer.start(null);
        ConnectionWatcher connectionWatcher = nettyConsumer.watchConnect(metadata);
        DemoService o = ProxyObjectFactory
                .factory()
                .consumer(nettyConsumer)
                .metadata(metadata)
                .isAsync(true)
                .connectWatch(connectionWatcher)
                .newProxyInstance(DemoService.class);
        o.hello("21");
        final InvokeFuture<String> future;
        try {
            future = InvokeFutureContext.get(String.class);
            future.addListener(new FutureListener<String>() {
                @Override
                public void completed(String result, Throwable throwable) throws Exception {
                    if (future.isSuccess()) {
                        System.out.println(result);
                    } else {
                        System.out.println(throwable.getMessage());
                    }
                }
            });
        } catch (InvokeFutureException e) {
            e.printStackTrace();
        }

        try {
            RepeatableFuture<String> watch = Listeners.onWatch("demo", DemoService.class, "hello", new Class<?>[]{String.class});
            watch.addListener(new FutureListener<String>() {
                @Override
                public void completed(String result, Throwable throwable) throws Exception {
                    System.out.println(Thread.currentThread().getName() + result);
                }
            });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        // 测试未引用同一个api
        ProxyObjectFactory
                .factory()
                .consumer(nettyConsumer)
                .metadata(metadata)
                .isAsync(true)
                .connectWatch(connectionWatcher)
                .remoteInvoke("DemoService", "hello", new Object[]{"232321"});
        InvokeFuture<String> objectFuture = InvokeFutureContext.get(String.class);
        objectFuture.addListener(new FutureListener<String>() {
            @Override
            public void completed(String result, Throwable throwable) throws Exception {
                System.out.println(Thread.currentThread().getName() + result);
            }
        });

        RepeatableFuture<Object> objectRepeatableFuture = Listeners.onWatch("demo", "DemoService", "msg", null);
        objectRepeatableFuture.addListener(new FutureListener<Object>() {
            @Override
            public void completed(Object result, Throwable throwable) throws Exception {
                System.out.println(Thread.currentThread().getName() + result);
            }
        });
        TimeUnit.SECONDS.sleep(1000);
    }

    interface DemoService {

        String hello(String msg);

        String msg();
    }

    @ProviderService(publishService = DemoService.class)
    class DemoServiceImpl implements DemoService {

        @Override
        public String hello(String msg) {
            System.out.println(msg);
            return msg;
        }

        @Override
        public String msg() {
            return "2221121";
        }
    }
}
