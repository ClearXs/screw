package com.jw.screw.monitor.opentracing;

import com.jw.screw.common.Status;
import com.jw.screw.common.transport.body.RequestBody;
import com.jw.screw.common.transport.body.ResponseBody;

/**
 * 链路追踪的一些常量
 * @author jiangw
 * @date 2020/12/24 21:31
 * @since 1.0
 */
public interface ScrewTracing {

    /**
     * transport code
     */
    String CODE = "code";

    /**
     * @see RequestBody#getInvokeId()
     */
    String INVOKE_ID = "invokeId";

    /**
     * @see RequestBody#getServiceName()
     */
    String SERVICE_NAME = "serviceName";

    /**
     * @see RequestBody#getMethodName()
     */
    String METHOD_NAME = "methodName";

    /**
     * @see RequestBody#getParameters()
     */
    String PARAMETERS = "parameters";

    /**
     * @see RequestBody#getExpectedReturnType()
     */
    String RETURN_TYPE = "returnType";

    /**
     * @see Status
     */
    String STATUS = "status";

    /**
     * @see ResponseBody#getResult()
     */
    String RESULT = "result";

    /**
     * @see ResponseBody#getError()
     */
    String ERROR = "error";
}
