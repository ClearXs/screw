package com.jw.screw.provider;

import com.jw.screw.common.Status;
import com.jw.screw.common.transport.body.RequestBody;
import com.jw.screw.common.transport.body.ResponseBody;
import com.jw.screw.provider.model.ServiceWrapper;
import com.jw.screw.remote.Protocol;
import com.jw.screw.remote.modle.RemoteTransporter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * screw
 * @author jiangw
 * @date 2020/12/10 14:13
 * @since 1.0
 */
public class NettyProviderRpcRequestProcessor extends AbstractRpcRequestProcessor {

    private static Logger logger = LoggerFactory.getLogger(NettyProviderRpcRequestProcessor.class);

    private final NettyProvider provider;

    public NettyProviderRpcRequestProcessor(NettyProvider provider) {
        this.provider = provider;
    }

    @Override
    public RemoteTransporter process(ChannelHandlerContext ctx, RemoteTransporter request) {
        RemoteTransporter remoteTransporter = null;
        // 解析request
        RequestBody requestBody = (RequestBody) request.getBody();
        // 请求的唯一id，确认消费与提供者
        long invokerId = requestBody.getInvokeId();
        // 请求的服务
        String serviceName = requestBody.getServiceName();
        // 请求的方法
        String methodName = requestBody.getMethodName();
        // 请求的参数
        Object[] parameters = requestBody.getParameters();
        // 从provider中找到注册的服务，如果找不到则发送失败请求
        ServiceWrapper serviceWrapper = provider.getServiceWrapperManager().wrapperContainer().lookupWrapper(serviceName);
        if (serviceWrapper == null) {
            remoteTransporter = rejected(Status.SERVICE_NOT_FOUND, request, serviceName, invokerId, null);
            return remoteTransporter;
        }
        try {
            Object generate = rpcInvokeGenerate(serviceWrapper, methodName, parameters);
            ResponseBody responseBody = new ResponseBody(invokerId);
            responseBody.setStatus(Status.OK.getValue());
            responseBody.setResult(generate);
            remoteTransporter = RemoteTransporter.createRemoteTransporter(Protocol.Code.RPC_RESPONSE, responseBody, request.getUnique());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            remoteTransporter = rejected(Status.SERVICE_INVOKE_ERROR, request, serviceName, invokerId, e);
            e.printStackTrace();
        }
        return remoteTransporter;
    }

    private RemoteTransporter rejected(Status status, final RemoteTransporter request, String serviceName, long invokeId, Throwable cause) {
        ResponseBody responseBody = new ResponseBody(invokeId);
        responseBody.setResult(null);
        switch (status) {
            case SERVICE_NOT_FOUND:
                responseBody.setStatus(status.getValue());
                responseBody.setError(serviceName + ": " + status.getDescription());
                break;
            case SERVICE_INVOKE_ERROR:
                responseBody.setStatus(status.getValue());
                responseBody.setError("invoke service errors：" + cause.getMessage());
            default:
                logger.warn("unexpected status: {}", status);
                break;
        }
        return RemoteTransporter.createRemoteTransporter(Protocol.Code.RPC_RESPONSE, responseBody, request.getUnique());
    }

}
