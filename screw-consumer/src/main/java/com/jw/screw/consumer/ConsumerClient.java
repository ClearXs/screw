package com.jw.screw.consumer;

import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.common.metadata.ServiceMetadata;
import com.jw.screw.common.model.Tuple;
import com.jw.screw.common.transport.body.AcknowledgeBody;
import com.jw.screw.common.transport.body.RegisterBody;
import com.jw.screw.common.util.Collections;
import com.jw.screw.monitor.api.ScrewMonitor;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.NettyClient;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 消费者客户端
 * @author jiangw
 * @date 2020/12/8 17:17
 * @since 1.0
 */
public class ConsumerClient extends NettyClient {

    private final NettyConsumer nettyConsumer;

    private ScrewMonitor monitor;

    public ConsumerClient(NettyConsumerConfig config, NettyConsumer nettyConsumer) {
        super(config.getRegistryConfig());
        this.nettyConsumer = nettyConsumer;
    }

    @Override
    protected void processAck(RemoteTransporter remoteTransporter) {
        if (remoteTransporter.getTransporterType() == Protocol.TransportType.ACK) {
            AcknowledgeBody acknowledgeBody = (AcknowledgeBody) remoteTransporter.getBody();
            nettyConsumer.nonAck.remove(acknowledgeBody.getSequence());
        }
    }

    @Override
    public void processRemoteResponse(ChannelHandlerContext ctx, RemoteTransporter request) {
        byte code = request.getCode();
        // 与RESPONSE_SUBSCRIBE使用同一个线程池与处理器
        if (code == Protocol.Code.SERVICE_OFFLINE) {
            code = Protocol.Code.RESPONSE_SUBSCRIBE;
        }
        // 监控客户端处理
        if (code == Protocol.Code.MONITOR_ADDRESS) {
            RegisterBody body = (RegisterBody) request.getBody();
            List<RegisterMetadata> registerMetadata = body.getRegisterMetadata();
            if (monitor != null && Collections.isNotEmpty(registerMetadata)) {
                monitor.setMonitorAddress(registerMetadata.toArray(new RegisterMetadata[] {}));
            } else if (monitor != null) {
                monitor.connectionAddress();
            }
        }
        Tuple<NettyProcessor, ExecutorService> tuple = processTables.get(code);
        if (tuple == null) {
            // 发送重试的Ack消息
            sendRetryAck(ctx, request);
            return;
        }
        final NettyProcessor processor = tuple.getKey();
        ExecutorService executorService = tuple.getValue();
        final ReentrantLock listenerLock = new ReentrantLock();
        if (code == Protocol.Code.RESPONSE_SUBSCRIBE || code == Protocol.Code.BROADCAST) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    listenerLock.lock();
                    try {
                        RemoteTransporter ackTransport = processor.process(ctx, request);
                        if (ackTransport != null) {
                            ctx.channel().writeAndFlush(ackTransport);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        listenerLock.unlock();
                    }
                }
            });
        } else {
            // 发送错误的Ack消息
            sendRetryAck(ctx, request);
        }
    }

    @Override
    protected ChannelHandler extraHandler() {
        // 与注册中心重连成功
        return new ChannelInboundHandlerAdapter() {

            /**
             * 如果是第一次连接，那么一定会进入该方法，在现有的判断下，需要加上重连次数。
             */
            private final ThreadLocal<AtomicInteger> counter = new ThreadLocal<>();

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                // 重新从注册中心订阅服务
                AtomicInteger reConnectCounter = counter.get();
                counter.remove();
                if (reConnectCounter == null) {
                    reConnectCounter = new AtomicInteger(0);
                }
                CopyOnWriteArraySet<ServiceMetadata> subscribeService = nettyConsumer.getSubscribeService();
                for (ServiceMetadata serviceMetadata : subscribeService) {
                    nettyConsumer.watchConnect(serviceMetadata);
                }
                counter.set(reConnectCounter);
            }
        };
    }

    public void setMonitor(ScrewMonitor monitor, String monitorServerKey) {
        addInboundFilter(new SubscribeFilter(monitorServerKey) {
            @Override
            protected void handle(List<RegisterMetadata> registerMetadata) {
                monitor.setMonitorAddress(registerMetadata.toArray(new RegisterMetadata[]{}));
            }
        });
        this.monitor = monitor;
    }
}
