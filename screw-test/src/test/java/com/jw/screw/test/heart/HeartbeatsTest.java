package com.jw.screw.test.heart;

import com.jw.screw.provider.NettyProvider;
import com.jw.screw.provider.NettyProviderConfig;
import com.jw.screw.provider.annotations.ProviderService;
import com.jw.screw.registry.DefaultRegistry;
import org.junit.Test;

public class HeartbeatsTest {

    @Test
    public void heartbeats() {
        DefaultRegistry defaultRegistry = new DefaultRegistry(8080);
        defaultRegistry.start();

    }

    @Test
    public void client() throws InterruptedException {
        NettyProviderConfig providerConfig = new NettyProviderConfig();
        providerConfig.setProviderKey("demo");
        providerConfig.setPort(8082);
        NettyProvider nettyProvider = new NettyProvider(providerConfig);
        nettyProvider.registry("localhost", 8080);
        nettyProvider.publishServices(new DemoServiceImpl());
        nettyProvider.start();
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
