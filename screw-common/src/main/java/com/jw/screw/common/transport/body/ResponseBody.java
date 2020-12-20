package com.jw.screw.common.transport.body;

/**
 * 响应body
 * @author jiangw
 * @date 2020/12/10 17:30
 * @since 1.0
 */
public class ResponseBody implements Body {

    /**
     * rpc调用的唯一id
     */
    private long invokeId;

    /**
     * 返回的状态
     */
    private byte status;

    /**
     * 返回的结果
     */
    private Object result;

    /**
     * 错误信息
     */
    private String error;

    public ResponseBody(long invokeId) {
        this.invokeId = invokeId;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getInvokeId() {
        return invokeId;
    }

    public void setInvokeId(long invokeId) {
        this.invokeId = invokeId;
    }

    @Override
    public String toString() {
        return "ResponseBody{" +
                "invokeId=" + invokeId +
                ", status=" + status +
                ", result=" + result +
                ", error='" + error + '\'' +
                '}';
    }
}
