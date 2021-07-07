package com.jw.screw.remote.netty;

import com.jw.screw.common.Status;
import com.jw.screw.common.exception.RemoteException;
import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.future.AbstractInvokeFuture;
import com.jw.screw.common.metadata.RegisterMetadata;
import com.jw.screw.common.model.Tuple;
import com.jw.screw.common.transport.body.AcknowledgeBody;
import com.jw.screw.common.transport.body.Body;
import com.jw.screw.common.transport.body.RegisterBody;
import com.jw.screw.common.transport.body.ResponseBody;
import com.jw.screw.common.util.Collections;
import com.jw.screw.common.util.Requires;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.RemoteService;
import com.jw.screw.remote.filter.*;
import com.jw.screw.remote.modle.RemotePromisor;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.config.GlobeConfig;
import com.jw.screw.remote.netty.processor.NettyProcessor;
import com.jw.screw.remote.netty.processor.NettyProcessors;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * abstract netty server implementation
 * 提供一般性的异步/同步调用。请求/响应前处理。过滤器链
 * @author jiangw
 * @date 2020/12/24 10:36
 * @since 1.0
 */
public abstract class AbstractNettyService implements RemoteService {

    private static Logger logger = LoggerFactory.getLogger(AbstractNettyService.class);

    /**
     * 请求-响应，他们使用的id都是相同的
     * 当请求来到时，根据请求的id put这个promisor对象
     * 当响应到来时，根据响应的id，get这个promisor对象并向 set promise对象
     */
    private final ConcurrentHashMap<Long, RemotePromisor> promises = new ConcurrentHashMap<>();

    /**
     * 类型{@link Protocol.Code}的线程处理器
     */
    protected ConcurrentHashMap<Byte, Tuple<NettyProcessor, ExecutorService>> processTables = new ConcurrentHashMap<>();

    protected ExecutorService defaultExecutors = NettyProcessors.defaultExec();

    /**
     * 入站过滤器
     */
    protected final List<Filter> inboundFilters = new CopyOnWriteArrayList<>();

    /**
     * 出站过滤器
     */
    protected final List<Filter> outboundFilters = new CopyOnWriteArrayList<>();

    public AbstractNettyService() {
        addOutboundFilter(new ProcessRequestFilter());
        addInboundFilter(new ProcessResponseFilter());
    }

    @Override
    public RemoteTransporter syncInvoke(Channel channel, final RemoteTransporter request, long timeoutMillis) throws InterruptedException, RemoteTimeoutException, RemoteSendException {
        final RemotePromisor promisor = new RemotePromisor(timeoutMillis);
        promises.put(request.getUnique(), promisor);
        try {
            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    boolean success = future.isSuccess();
                    logger.info("send successful: {}", success);
                    promisor.setSendSuccessful(success);
                    if (!success) {
                        promises.remove(request.getUnique());
                    }
                    promisor.setCause(future.cause());
                }
            });
            RemoteTransporter transporter = promisor.getTransporter();
            if (transporter == null) {
                if (promisor.isSendSuccessful()) {
                    throw new RemoteTimeoutException(channel.remoteAddress().toString(), timeoutMillis, promisor.getCause());
                } else {
                    throw new RemoteSendException(channel.remoteAddress().toString(), promisor.getCause());
                }
            }
            return transporter;
        } finally {
            // 保证能够移除，使内存不至于撑爆
            promises.remove(request.getUnique());
        }
    }

    @Override
    public AsyncPromise<?> asyncInvoke(Channel channel, RemoteTransporter request, Class<?> returnType) {
        final RemotePromisor promisor = new RemotePromisor();
        promises.put(request.getUnique(), promisor);
        final ChannelFuture channelFuture = channel.writeAndFlush(request);
        return new AsyncPromise(returnType, new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    channelFuture.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            boolean success = future.isSuccess();
                            promisor.setSendSuccessful(success);
                            if (!success) {
                                promises.remove(request.getUnique());
                                promisor.setCause(future.cause());
                            }
                        }
                    });
                    RemoteTransporter transporter = promisor.getTransporter();
                    if (promisor.isSendSuccessful() && transporter != null) {
                        ResponseBody responseBody = (ResponseBody) transporter.getBody();
                        byte status = responseBody.getStatus();
                        if (status == Status.OK.getValue()) {
                            return responseBody.getResult();
                        }
                        throw new RemoteException(responseBody.getError());
                    }
                    throw new RemoteException(promisor.getCause());
                } finally {
                    promises.remove(request.getUnique());
                }
            }
        });
    }

    @Override
    public void processRemoteRequest(final ChannelHandlerContext ctx, final RemoteTransporter remoteTransporter) {
        byte code = remoteTransporter.getCode();
        Tuple<NettyProcessor, ExecutorService> tuple = processTables.get(code);
        // 如果当前的code还没有，使用默认处理器
        if (tuple == null) {
            tuple = NettyProcessors.failProcess(new NullPointerException("unknown code: " + code));
        }
        final NettyProcessor processor = tuple.getKey();
        ExecutorService executor = tuple.getValue();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                // 开启处理业务逻辑
                RemoteTransporter response = processor.process(ctx, remoteTransporter);
                if (response != null) {
                    // 回写响应
                    ctx.channel().writeAndFlush(response).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("send response successful. business handle successful");
                                }
                            } else {
                                if (logger.isWarnEnabled()) {
                                    logger.warn("send response error, {}", future.cause().getMessage());
                                    future.cause().printStackTrace();
                                }
                            }
                        }
                    });
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("business handler return result is empty...");
                    }
                }
            }
        });
    }

    @Override
    public void processRemoteResponse(ChannelHandlerContext ctx, RemoteTransporter remoteTransporter) {
        RemotePromisor promisor = promises.get(remoteTransporter.getUnique());
        if (promisor != null) {
            // operationComplete与response不是同步的，所以手动设置为true
            promisor.setSendSuccessful(true);
            promisor.putTransporter(remoteTransporter);
            // 移除当前的promisor对象
            promises.remove(remoteTransporter.getUnique());
        }
    }

    /**
     * 处理请求与响应
     * 网络请求有两种情况：
     * 1.远程请求：注册请求、订阅请求、远程调用请求。
     * 2.响应来临：远程调用的响应。
     * 3.过滤器链
     * 区分这两种情况，根据传输对象的类型进行判断（也可以通过handler）
     * @param ctx {@link ChannelHandlerContext}
     * @param remoteTransporter {@link RemoteTransporter}
     */
    protected void doRequestAndResponse(ChannelHandlerContext ctx, RemoteTransporter remoteTransporter) {
        switch (remoteTransporter.getTransporterType()) {
            case Protocol.TransportType.REMOTE_REQUEST:
                // 出站过滤器
                processRequestAndResponse(ctx, remoteTransporter, outboundFilters);
                break;
            case Protocol.TransportType.REMOTE_RESPONSE:
                // 入站过滤器
                processRequestAndResponse(ctx, remoteTransporter, inboundFilters);
                break;
            case Protocol.TransportType.ACK:
                // 处理Ack的传输
                processAck(remoteTransporter);
            default:
                break;
        }
    }

    @Override
    public void shutdownProcessors() {
        if (processTables.size() > 0) {
            for (Map.Entry<Byte, Tuple<NettyProcessor, ExecutorService>> tupleEntry : processTables.entrySet()) {
                Tuple<NettyProcessor, ExecutorService> tuple = tupleEntry.getValue();
                ExecutorService executor = tuple.getValue();
                executor.shutdown();
            }
        }
    }

    @Override
    public void registerProcessors(byte code, NettyProcessor processor, ExecutorService exec) {
        if (exec == null) {
            exec = defaultExecutors;
        }
        Tuple<NettyProcessor, ExecutorService> tuple = new Tuple<>(processor, exec);
        processTables.put(code, tuple);
    }

    protected void sendRetryAck(ChannelHandlerContext ctx, RemoteTransporter request) {
        // 发送错误的Ack消息
        AcknowledgeBody retryAck = new AcknowledgeBody(request.getUnique(), true);
        ctx.channel().writeAndFlush(RemoteTransporter
                .createRemoteTransporter(Protocol.Code.UNKNOWN, retryAck, request.getUnique(), Protocol.TransportType.ACK))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public synchronized void addInboundFilter(Filter... filters) {
        inboundFilters.addAll(addFilter(inboundFilters, filters));
    }

    @Override
    public synchronized void addOutboundFilter(Filter... filters) {
        outboundFilters.addAll(addFilter(outboundFilters, filters));
    }

    protected void processRequestAndResponse(ChannelHandlerContext ctx, RemoteTransporter remoteTransporter, List<Filter> filters) {
        if (Collections.isNotEmpty(filters)) {
            FilterChain chain = FilterChainLoader.loadChain(filters.toArray(new Filter[] {}));
            if (chain != null) {
                try {
                    chain.process(remoteTransporter.getBody(), new FilterContext(this, ctx, remoteTransporter));
                } catch (InterruptedException | RemoteTimeoutException | RemoteSendException e) {
                    logger.warn("filter chain process error: {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 处理Ack
     * 消费者、提供者、注册中心：实现Ack消息处理机制
     * @param remoteTransporter {@link RemoteTransporter}
     */
    protected abstract void processAck(RemoteTransporter remoteTransporter);

    /**
     * 子类支持自定义的处理器，实现解耦
     * @return {@link ChannelHandler}
     */
    protected abstract ChannelHandler extraHandler();

    /**
     * 向过滤器集合添加过滤器，如果在集合中存在，那么不进行添加
     * @param boundFilters {@link List<Filter>}
     * @param filters 过滤器{@link Filter}
     * @return {@link List<Filter>}
     */
    private List<Filter> addFilter(List<Filter> boundFilters, Filter... filters) {
        List<Filter> additional = new ArrayList<>();
        for (Filter filter : filters) {
            if (!boundFilters.contains(filter)) {
                additional.add(filter);
            }
        }
        return additional;
    }

    /**
     * 异步处理 future
     * @param <T>
     */
    static class AsyncPromise<T> extends AbstractInvokeFuture<T> {

        private final Class<T> realClass;

        AsyncPromise(Class<T> realClass, Callable<T> callable) {
            super();
            Requires.isNull(realClass, "return type");
            Requires.isNull(callable, "callable");
            this.callable = callable;
            this.realClass = realClass;
            taskExecutor.execute(this);
        }

        @Override
        public Class<T> realClass() {
            return realClass;
        }

        @Override
        public T getResult() throws ExecutionException, InterruptedException, TimeoutException {
            return getResult(GlobeConfig.CONNECT_TIMEOUT_MILLIS);
        }

        @Override
        public T getResult(long millis) throws InterruptedException, ExecutionException, TimeoutException {
            try {
                return realClass.cast(get(millis, TimeUnit.MILLISECONDS));
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * 根据构造参数provider-key，获取相应的注册表信息{@link RegisterMetadata}并做相应处理
     */
    protected abstract static class SubscribeFilter extends AbstractFilter {

        private final List<String> providerKey;

        public SubscribeFilter(String... providerKey) {
            this.providerKey = Arrays.asList(providerKey);
        }

        @Override
        public <T extends FilterContext> void doFilter(Body body, T context, FilterChain next) throws InterruptedException, RemoteTimeoutException, RemoteSendException {
            if (body instanceof RegisterBody) {
                RegisterBody registerBody = (RegisterBody) body;
                List<RegisterMetadata> registerMetadata = registerBody.getRegisterMetadata();
                if (Collections.isNotEmpty(registerMetadata)) {
                    List<RegisterMetadata> collectRegister = registerMetadata.stream()
                            .filter(o -> providerKey.contains(o.getServiceProviderName())).collect(Collectors.toList());
                    if (Collections.isNotEmpty(collectRegister)) {
                        handle(collectRegister);
                    }
                }
            }
            if (next != null) {
                next.process(body, context);
            }
        }

        /**
         * 处理动作，比如再次连接监控中心
         * @param registerMetadata 注册元数据集合{@link RegisterMetadata}
         */
        protected abstract void handle(List<RegisterMetadata> registerMetadata);
    }
}
