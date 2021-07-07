package com.jw.screw.test.monitor;

import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.consumer.NettyConsumer;
import com.jw.screw.consumer.NettyConsumerConfig;
import com.jw.screw.monitor.remote.MonitorProvider;
import com.jw.screw.provider.NettyProvider;
import com.jw.screw.provider.NettyProviderConfig;
import com.jw.screw.provider.annotations.ProviderService;
import com.jw.screw.registry.DefaultRegistry;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MonitorTest {

    @Test
    public void registry() {
        DefaultRegistry defaultRegistry = new DefaultRegistry(8080);
        defaultRegistry.start();
    }

    @Test
    public void monitor() throws InterruptedException, ConnectionException, ExecutionException {
        NettyProviderConfig monitorConfig = new NettyProviderConfig();
        monitorConfig.setServerKey("monitor");
        monitorConfig.setWeight(4);
        monitorConfig.setPort(8083);
        MonitorProvider monitorProvider = new MonitorProvider(monitorConfig);
        monitorProvider.registry(new RemoteAddress("localhost", 8080));
        monitorProvider.start();
        TimeUnit.SECONDS.sleep(3000);
    }

    @Test
    public void provider() throws InterruptedException, ConnectionException, ExecutionException {
        NettyProviderConfig providerConfig = new NettyProviderConfig();
        providerConfig.setServerKey("provider");
        providerConfig.setWeight(4);
        providerConfig.setPort(8082);
        providerConfig.setMonitorServerKey("monitor");
        final NettyProvider nettyProvider = new NettyProvider(providerConfig);
        nettyProvider.registry(new RemoteAddress("localhost", 8080));
        nettyProvider.publishServices(new DemoServiceImpl());
        nettyProvider.start();
        TimeUnit.SECONDS.sleep(3000);

    }

    @Test
    public void consumer() throws ConnectionException, InterruptedException, ExecutionException {
        NettyConsumerConfig nettyConsumerConfig = new NettyConsumerConfig();
        nettyConsumerConfig.setServerKey("consumer");
        nettyConsumerConfig.setMonitorServerKey("monitor");
        NettyConsumer nettyConsumer = new NettyConsumer(nettyConsumerConfig);
        nettyConsumer.register("localhost", 8080);
        nettyConsumer.start(null);

        TimeUnit.SECONDS.sleep(100);
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
