package com.jw.screw.remote.netty.config;

import com.jw.screw.remote.SConfig;

public interface GlobeConfig extends SConfig {

    /**
     * 连接超时时间
     */
    int CONNECT_TIMEOUT_MILLIS = 3000;

}
