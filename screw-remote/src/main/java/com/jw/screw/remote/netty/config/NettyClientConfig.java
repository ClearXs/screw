package com.jw.screw.remote.netty.config;

import com.jw.screw.common.transport.UnresolvedAddress;
import com.jw.screw.remote.SConfig;

/**
 * netty-Client的一些配置项
 * 1.地址
 * 2.工作线程核心数
 * @author jiangw
 * @date 2020/11/26 9:30
 * @since 1.0
 */
public class NettyClientConfig implements SConfig {

    private UnresolvedAddress defaultAddress;

    private final int workThreads = Runtime.getRuntime().availableProcessors() << 1;

    public UnresolvedAddress getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(UnresolvedAddress defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public int getWorkThreads() {
        return workThreads;
    }
}
