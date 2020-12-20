package com.jw.screw.consumer.filter;


import com.jw.screw.common.future.InvokeFuture;
import com.jw.screw.consumer.invoker.Invoker;
import com.jw.screw.remote.netty.NettyClient;

/**
 * screw
 * @author jiangw
 * @date 2020/12/8 17:18
 * @since 1.0
 */
public class FilterContext {

    private Invoker invoker;

    private Object result;

    private NettyClient rpcClient;

    private Class<?> returnType;

    private InvokeFuture<?> future;

    public FilterContext(Invoker invoker, NettyClient rpcClient) {
        this.invoker = invoker;
        this.rpcClient = rpcClient;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setFuture(InvokeFuture<?> future) {
        this.future = future;
    }

    public InvokeFuture<?> getFuture() {
        return future;
    }

    public NettyClient getRpcClient() {
        return rpcClient;
    }

    public void setRpcClient(NettyClient rpcClient) {
        this.rpcClient = rpcClient;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }
}
