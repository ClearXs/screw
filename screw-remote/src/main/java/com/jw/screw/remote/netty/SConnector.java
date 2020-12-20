package com.jw.screw.remote.netty;

import com.jw.screw.common.exception.ConnectionException;
import com.jw.screw.common.transport.UnresolvedAddress;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public interface SConnector extends Comparable<SConnector>{

    UnresolvedAddress address();

    /**
     * 设置是否重连
     * @param reConnect
     */
    void setReConnect(boolean reConnect);

    /**
     * 设置channelFuture
     * @param channelFuture
     */
    void setChannelFuture(ChannelFuture channelFuture);

    /**
     * 创建一个channel
     * @return
     * @throws ConnectionException
     */
    Channel createChannel() throws ConnectionException;

    ChannelGroup channelGroup();

    /**
     * <p>
     *     1.关闭{@link ChannelGroup}中所有的{@link Channel}
     * </p>
     * <p>
     *     2.移除remoteChannels的通道缓存数据
     * </p>
     */
    void close() throws InterruptedException;

    /**
     * 权重
     * @return
     */
    int weight();
}
