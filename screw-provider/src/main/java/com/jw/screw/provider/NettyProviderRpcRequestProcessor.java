package com.jw.screw.provider;

import com.jw.screw.common.Status;
import com.jw.screw.common.constant.StringPool;
import com.jw.screw.common.exception.ExceptionTraceStack;
import com.jw.screw.common.transport.body.RequestBody;
import com.jw.screw.common.transport.body.ResponseBody;
import com.jw.screw.monitor.opentracing.ScrewTracer;
import com.jw.screw.monitor.opentracing.TracerCache;
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
            remoteTransporter = rejected(Status.SERVICE_NOT_FOUND, request, serviceName, methodName, invokerId, null);
            return remoteTransporter;
        }
        // tracer放入缓存中
        TracerCache.put((ScrewTracer) requestBody.getAttached());
        try {
            Object generate = rpcInvokeGenerate(serviceWrapper, methodName, parameters);
            ResponseBody responseBody = new ResponseBody(invokerId);
            responseBody.setStatus(Status.OK.getValue());
            responseBody.setResult(generate);
            responseBody.attachment(requestBody.getAttached());
            // 清除缓存
            TracerCache.clear();
            remoteTransporter = RemoteTransporter.createRemoteTransporter(Protocol.Code.RPC_RESPONSE, responseBody, request.getUnique());
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                remoteTransporter = rejected(Status.SERVICE_INVOKE_ERROR, request, serviceName, methodName, invokerId, e);
            } else if (e instanceof NoSuchMethodException) {
                remoteTransporter = rejected(Status.SERVICE_NOT_FOUND, request, serviceName, methodName, invokerId, e);
            } else if (e instanceof IllegalAccessException) {
                remoteTransporter = rejected(Status.SERVICE_ILLEGAL_ACCESS, request, serviceName, methodName, invokerId, e);
            }
            e.printStackTrace();
        }
        return remoteTransporter;
    }

    private RemoteTransporter rejected(Status status, final RemoteTransporter request, String serviceName, String methodName, long invokeId, Exception e) {
        RequestBody requestBody = (RequestBody) request.getBody();
        ResponseBody responseBody = new ResponseBody(invokeId);
        responseBody.setResult(null);
        switch (status) {
            case SERVICE_NOT_FOUND:
                responseBody.setStatus(status.getValue());
                responseBody.setError(serviceName + StringPool.AT + methodName + StringPool.COLON +  status.getDescription());
                break;
            case SERVICE_INVOKE_ERROR:
                responseBody.setStatus(status.getValue());
                responseBody.setError(Status.SERVICE_INVOKE_ERROR.getDescription() + StringPool.COLON + e.getMessage());
                if (e instanceof InvocationTargetException) {
                    responseBody.setExceptionTrace(buildExceptionStack((InvocationTargetException) e));
                }
                break;
            case SERVICE_ILLEGAL_ACCESS:
                responseBody.setStatus(status.getValue());
                responseBody.setError(serviceName + StringPool.AT + methodName + StringPool.COLON +  status.getDescription());
                break;
            default:
                logger.warn("unexpected status: {}", status);
                break;
        }
        responseBody.attachment(requestBody.getAttached());
        TracerCache.clear();
        return RemoteTransporter.createRemoteTransporter(Protocol.Code.RPC_RESPONSE, responseBody, request.getUnique());
    }

    private ExceptionTraceStack buildExceptionStack(InvocationTargetException e) {
        Throwable targetException = e.getTargetException();
        ExceptionTraceStack exceptionTraceStack = new ExceptionTraceStack();
        exceptionTraceStack.setCause(targetException.getClass().getName() + StringPool.COLON + targetException.getMessage());
        StackTraceElement[] stackTrace = targetException.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            exceptionTraceStack.addTraceStack(StringPool.ATINC + " " +  stackTraceElement.toString());
        }
        return exceptionTraceStack;
    }
}
