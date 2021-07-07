package com.jw.screw.remote;

import com.jw.screw.remote.netty.ChannelGroup;

/**
 * screw
 * @author jiangw
 * @date 2020/12/8 17:18
 * @since 1.0
 */
public abstract class AbstractInvoker implements Invoker {

    protected ChannelGroup channelGroup;

    public AbstractInvoker(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }
}
