package com.jw.screw.registry;

import com.jw.screw.remote.netty.config.NettyServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jiangw
 * @date 2020/12/8 11:50
 * @since 1.0
 */
public abstract class AbstractRegistry implements Registry {

    private static Logger logger = LoggerFactory.getLogger(AbstractRegistry.class);

    /**
     * 注册中心上下文对象
     */
    protected final RegistryContext registryContext;

    /**
     * 作为注册中心的netty服务端
     */
    protected final NettyRegistryServer registerServer;

    protected NettyServerConfig registryConfig;

    public AbstractRegistry(int port) {
        registryConfig = new NettyServerConfig(port);
        registryContext = new RegistryContext();
        registerServer = new NettyRegistryServer(registryConfig, registryContext);
    }

    @Override
    public void shutdown() {
        if (logger.isDebugEnabled()) {
            logger.info("shutdown register...");
        }
        registerServer.shutdownGracefully();
    }
}
