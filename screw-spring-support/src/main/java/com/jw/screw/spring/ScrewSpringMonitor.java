package com.jw.screw.spring;

import com.jw.screw.common.NamedThreadFactory;
import com.jw.screw.common.SystemConfig;
import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.exception.RemoteException;
import com.jw.screw.common.transport.RemoteAddress;
import com.jw.screw.common.util.Requires;
import com.jw.screw.monitor.remote.MonitorProvider;
import com.jw.screw.provider.NettyProviderConfig;
import com.jw.screw.common.util.Remotes;
import com.jw.screw.remote.netty.config.GlobeConfig;
import com.jw.screw.remote.netty.config.NettyClientConfig;
import com.jw.screw.spring.anntation.ScrewValue;
import com.jw.screw.spring.config.Property;
import com.jw.screw.spring.config.TypePropertySource;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * @see com.jw.screw.monitor.remote.MonitorProvider
 * @author jiangw
 * @date 2020/12/25 19:44
 * @since 1.0
 */
public class ScrewSpringMonitor implements ScrewSpring {

    /**
     * 注册中心host
     */
    @ScrewValue("monitor-registry.address")
    private String registryAddress;

    /**
     * 注册中心port
     */
    @ScrewValue("monitor-registry.port")
    private Integer registryPort;

    /**
     * 监控地址
     */
    @ScrewValue("monitor-monitor.address")
    private String monitorAddress;

    /**
     * 服务权重
     */
    @ScrewValue("monitor-weight")
    private Integer weight;

    /**
     * 最多支持连接数
     */
    @ScrewValue("monitor-connCount")
    private Integer connCount;

    /**
     * 配置中心
     */
    @ScrewValue("provider-config.key")
    private String configServerKey;

    /**
     * @see MonitorProvider
     */
    private MonitorProvider monitorProvider;

    /**
     * @see NettyProviderConfig
     */
    private NettyProviderConfig monitorConfig;

    private final ExecutorService monitor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(), new NamedThreadFactory("screw monitor"));

    @Override
    public void initConfig() throws IOException, InterruptedException, RemoteException {
        // 基本配置
        monitorConfig.setServerKey(SystemConfig.MONITOR_CENTER.getRole());
        monitorConfig.setPort(SystemConfig.MONITOR_CENTER.getDefaultPort());
        monitorConfig.setWeight(weight);
        monitorConfig.setConnCount(connCount);
        monitorConfig.setRole(SystemConfig.MONITOR_CENTER.getRole());

        NettyClientConfig registryClientConfig = new NettyClientConfig();
        // 判断注册中心是否可连接
        boolean connectable = Remotes.connectable(registryAddress, registryPort, GlobeConfig.CONNECT_TIMEOUT_MILLIS);
        if (!connectable) {
            // 阻塞一段时间，再次尝试
            connectable = Remotes.connectable(registryAddress, registryPort, GlobeConfig.CONNECT_TIMEOUT_MILLIS);
            if (!connectable) {
                throw new RemoteException("registry center can't connect");
            }
        }
        registryClientConfig.setDefaultAddress(new RemoteAddress(registryAddress, registryPort));
        monitorConfig.setServerHost(monitorAddress);
        monitorConfig.setRegisterConfig(registryClientConfig);
        // 配置中心地址
        monitorConfig.setConfigServerKey(configServerKey);
    }

    @Override
    public void validateParams() {
        // 验证参数
        Requires.isNull(registryAddress, "registryAddress");
        Requires.isNull(registryPort, "registryPort");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        validateParams();
        monitorConfig = new NettyProviderConfig();
        initConfig();
        monitorProvider = new MonitorProvider(monitorConfig);
        monitor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    monitorProvider.start();
                } catch (InterruptedException | ConnectionException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Environment environment = applicationContext.getEnvironment();
        if (environment instanceof ConfigurableEnvironment) {
            PropertySource<?> propertySource = ((ConfigurableEnvironment) environment).getPropertySources().get("monitor");
            if (propertySource != null) {
                Property.addResource(new TypePropertySource<>(propertySource));
                Property.refreshProperties(this);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if (monitorProvider != null) {
            monitorProvider.shutdown();
        }
        monitor.shutdown();
    }

    public MonitorProvider getMonitorProvider() {
        return monitorProvider;
    }

    public NettyProviderConfig getMonitorConfig() {
        return monitorConfig;
    }
}
