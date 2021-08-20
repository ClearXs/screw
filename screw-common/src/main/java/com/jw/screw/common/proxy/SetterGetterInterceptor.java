package com.jw.screw.common.proxy;

public interface SetterGetterInterceptor {

    InvocationInterceptor getInterceptor();

    void setInterceptor(InvocationInterceptor interceptor);
}
