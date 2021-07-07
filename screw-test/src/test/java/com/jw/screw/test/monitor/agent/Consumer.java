package com.jw.screw.test.monitor.agent;

import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.consumer.ConnectionWatcher;
import com.jw.screw.consumer.NettyConsumer;
import com.jw.screw.consumer.NettyConsumerConfig;
import com.jw.screw.consumer.model.ProxyObjectFactory;
import com.jw.screw.test.monitor.DemoService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Consumer {

    public static void main(String[] args) throws ConnectionException, InterruptedException, ExecutionException {
        NettyConsumerConfig nettyConsumerConfig = new NettyConsumerConfig();
        nettyConsumerConfig.setServerKey("consumer");
        nettyConsumerConfig.setMonitorServerKey("monitor");
        NettyConsumer nettyConsumer = new NettyConsumer(nettyConsumerConfig);
        nettyConsumer.register("localhost", 8080);
        nettyConsumer.start(null);
        ServiceMetadata metadata = new ServiceMetadata("provider");
        ConnectionWatcher connectionWatcher = nettyConsumer.watchConnect(metadata);
        DemoService o = ProxyObjectFactory
                .factory()
                .consumer(nettyConsumer)
                .metadata(metadata)
                .isAsync(false)
                .connectWatch(connectionWatcher)
                .newProxyInstance(DemoService.class);
        String ad = o.hello("ad");
        System.out.println(ad);
        TimeUnit.SECONDS.sleep(1000);
    }
}
