package com.jw.screw.remote.netty.processor;

import com.jw.screw.remote.modle.RemoteTransporter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 针对某一类类型创建其处理器
 * 远程调用请求，解析请求，调用服务
 * 注册请求
 * 订阅定期
 * @author jiangw
 * @date 2020/11/26 17:08
 * @since 1.0
 */
public interface NettyProcessor {

    RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request);
}
