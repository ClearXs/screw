package com.jw.screw.test.loadbalance;

import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.exception.InvokeFutureException;
import com.jw.screw.common.future.FutureListener;
import com.jw.screw.common.future.InvokeFuture;
import com.jw.screw.common.future.InvokeFutureContext;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.consumer.ConnectWatch;
import com.jw.screw.consumer.NettyConsumer;
import com.jw.screw.consumer.model.ProxyObjectFactory;
import com.jw.screw.provider.NettyProvider;
import com.jw.screw.provider.NettyProviderConfig;
import com.jw.screw.provider.annotations.ProviderService;
import com.jw.screw.registry.DefaultRegistry;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoadBalanceTest {

    @Test
    public void register() {
        DefaultRegistry defaultRegistry = new DefaultRegistry(8080);
        defaultRegistry.start();
    }

    @Test
    public void provider1() throws InterruptedException {
        NettyProviderConfig providerConfig = new NettyProviderConfig();
        providerConfig.setProviderKey("demo");
        providerConfig.setWeight(3);
        providerConfig.setPort(8081);
        NettyProvider nettyProvider = new NettyProvider(providerConfig);
        nettyProvider.publishServices(new DemoServiceImpl());
//        nettyProvider.registry(new RemoteAddress("localhost", 8080));
        nettyProvider.start();

        TimeUnit.SECONDS.sleep(100);
        nettyProvider.shutdown();
    }

    @Test
    public void provider2() throws InterruptedException {
        NettyProviderConfig providerConfig = new NettyProviderConfig();
        providerConfig.setProviderKey("demo");
        providerConfig.setWeight(4);
        providerConfig.setPort(8082);
        NettyProvider nettyProvider = new NettyProvider(providerConfig);
        nettyProvider.publishServices(new DemoServiceImpl());
        nettyProvider.registry(new RemoteAddress("localhost", 8080));
        nettyProvider.start();

        TimeUnit.SECONDS.sleep(100);
        nettyProvider.shutdown();
    }

    @Test
    public void demoConsumerTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                NettyConsumer nettyConsumer = new NettyConsumer();
                nettyConsumer.register("localhost", 8080);
                try {
                    nettyConsumer.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ServiceMetadata metadata = new ServiceMetadata("demo");
                ConnectWatch connectWatch = null;
                try {
                    connectWatch = nettyConsumer.watchConnect(metadata);
                } catch (InterruptedException | ConnectionException e) {
                    e.printStackTrace();
                }
                DemoService o = ProxyObjectFactory
                        .factory()
                        .consumer(nettyConsumer)
                        .connectWatch(connectWatch)
                        .metadata(metadata)
                        .newProxyInstance(DemoService.class);
                String hello = o.hello("2");
                System.out.println(hello);
            }
        });
        TimeUnit.SECONDS.sleep(100);
    }


    @Test
    public void asyncTest() throws ConnectionException, InterruptedException, ExecutionException {
        NettyConsumer nettyConsumer = new NettyConsumer();
        ServiceMetadata metadata = new ServiceMetadata("demo");

        nettyConsumer.directService(metadata, "localhost", 8081);
        DemoService o = ProxyObjectFactory
                .factory()
                .consumer(nettyConsumer)
                .metadata(metadata)
                .isAsync(true)
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
        TimeUnit.SECONDS.sleep(1000);
    }

    @Test
    public void asyncTest2() throws ConnectionException, InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        NettyConsumer nettyConsumer = new NettyConsumer();
                        ServiceMetadata metadata = new ServiceMetadata("demo");
                        nettyConsumer.directService(metadata, "localhost", 8081);
                        DemoService o = ProxyObjectFactory
                                .factory()
                                .consumer(nettyConsumer)
                                .metadata(metadata)
                                .isAsync(true)
                                .newProxyInstance(DemoService.class);
                        o.hello("2321" + finalI);
                        final InvokeFuture<String> future = InvokeFutureContext.get(String.class);
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
                    } catch (InterruptedException | ConnectionException | InvokeFutureException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        TimeUnit.SECONDS.sleep(1000);
    }


    interface DemoService {

        String hello(String msg);
    }

    @ProviderService(publishService = DemoService.class)
    class DemoServiceImpl implements DemoService {

        @Override
        public String hello(String msg) {
            System.out.println(msg);
            return msg;
        }
    }
}
