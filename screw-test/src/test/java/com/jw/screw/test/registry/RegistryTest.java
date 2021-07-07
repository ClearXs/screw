package com.jw.screw.test.registry;

import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.consumer.ConnectionWatcher;
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

public class RegistryTest {

    @Test
    public void register() {
        DefaultRegistry defaultRegistry = new DefaultRegistry(8080);
        defaultRegistry.start();
    }

    @Test
    public void DemoProviderTest() throws InterruptedException, ConnectionException, ExecutionException {
        NettyProviderConfig providerConfig = new NettyProviderConfig();
        providerConfig.setServerKey("demo");
        providerConfig.setPort(8082);
        NettyProvider nettyProvider = new NettyProvider(providerConfig);
        nettyProvider.publishServices(new DemoServiceImpl());
        nettyProvider.registry(new RemoteAddress("localhost", 8080));
        nettyProvider.start();

        TimeUnit.SECONDS.sleep(100);
        nettyProvider.shutdown();
    }

    @Test
    public void DemoConsumerTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                NettyConsumer nettyConsumer = new NettyConsumer();
                nettyConsumer.register("localhost", 8080);
                try {
                    nettyConsumer.start(null);
                } catch (InterruptedException | ConnectionException | ExecutionException e) {
                    e.printStackTrace();
                }
                ServiceMetadata metadata = new ServiceMetadata("demo");
                ConnectionWatcher connectionWatcher = null;
                try {
                    connectionWatcher = nettyConsumer.watchConnect(metadata);
                } catch (InterruptedException | ConnectionException e) {
                    e.printStackTrace();
                }
                DemoService o = ProxyObjectFactory
                        .factory()
                        .consumer(nettyConsumer)
                        .connectWatch(connectionWatcher)
                        .metadata(metadata)
                        .newProxyInstance(DemoService.class);
                String hello = o.hello("2");
                System.out.println(hello);
            }
        });
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
