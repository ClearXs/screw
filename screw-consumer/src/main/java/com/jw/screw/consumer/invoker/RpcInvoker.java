package com.jw.screw.consumer.invoker;

import com.jw.screw.common.exception.RemoteSendException;
import com.jw.screw.common.exception.RemoteTimeoutException;
import com.jw.screw.common.future.InvokeFuture;
import com.jw.screw.common.future.InvokeFutureContext;
import com.jw.screw.common.transport.body.Body;
import com.jw.screw.common.transport.body.RequestBody;
import com.jw.screw.common.transport.body.ResponseBody;
import com.jw.screw.remote.AbstractInvoker;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.RemoteService;
import com.jw.screw.remote.modle.RemoteTransporter;
import com.jw.screw.remote.netty.ChannelGroup;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * screw
 * @author jiangw
 * @date 2020/12/8 17:19
 * @since 1.0
 */
public class RpcInvoker extends AbstractInvoker {

    private static Logger logger = LoggerFactory.getLogger(RpcInvoker.class);

    private final boolean async;

    public RpcInvoker(ChannelGroup channelGroup, boolean async) {
        super(channelGroup);
        this.async = async;
    }

    @Override
    public Object invoke(Body body, RemoteService remoteService) throws InterruptedException, RemoteTimeoutException, RemoteSendException {
        if (body instanceof RequestBody) {
            RequestBody request = (RequestBody) body;
            Channel channel = channelGroup.next();
            RemoteTransporter remoteTransporter = new RemoteTransporter();
            remoteTransporter.setBody(request);
            remoteTransporter.setCode(Protocol.Code.RPC_REQUEST);
            remoteTransporter.setTransporterType(Protocol.TransportType.REMOTE_REQUEST);
            if (logger.isDebugEnabled()) {
                logger.debug("rpc invoke target {}, invoke object {}", channel, remoteTransporter);
            }
            if (async) {
                Object o = remoteService.asyncInvoke(channel, remoteTransporter, request.getExpectedReturnType());
                InvokeFutureContext.set((InvokeFuture<?>) o);
                return o;
            }
            RemoteTransporter response = remoteService.syncInvoke(channel, remoteTransporter, 30000);
            if (response != null) {
                ResponseBody responseBody = (ResponseBody) response.getBody();
                return responseBody.getResult();
            } else {
                throw new RemoteSendException("rpc invoker error");
            }
        }
        return null;
    }
}
