package com.jw.screw.test.monitor.agent;

import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.provider.NettyProvider;
import com.jw.screw.provider.NettyProviderConfig;
import com.jw.screw.test.monitor.DemoServiceImpl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Provider {

    public static void main(String[] args) throws ConnectionException, InterruptedException, ExecutionException {
        NettyProviderConfig providerConfig = new NettyProviderConfig();
        providerConfig.setServerKey("provider");
        providerConfig.setWeight(4);
        providerConfig.setPort(8081);
        providerConfig.setMonitorServerKey("monitor");
        NettyProvider nettyProvider = new NettyProvider(providerConfig);
        nettyProvider.registry(new RemoteAddress("localhost", 8080));
        nettyProvider.publishServices(new DemoServiceImpl());
        nettyProvider.start();
        TimeUnit.SECONDS.sleep(3000);
    }
}
