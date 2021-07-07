package com.jw.screw.test.monitor.agent;

import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.monitor.remote.MonitorProvider;
import com.jw.screw.provider.NettyProviderConfig;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Monitor {

    public static void main(String[] args) throws ConnectionException, InterruptedException, ExecutionException {
        NettyProviderConfig monitorConfig = new NettyProviderConfig();
        monitorConfig.setServerKey("monitor");
        monitorConfig.setWeight(4);
        monitorConfig.setPort(8082);
        MonitorProvider monitorProvider = new MonitorProvider(monitorConfig);
        monitorProvider.registry(new RemoteAddress("localhost", 8080));
        monitorProvider.start();
        TimeUnit.SECONDS.sleep(3000);
    }
}
