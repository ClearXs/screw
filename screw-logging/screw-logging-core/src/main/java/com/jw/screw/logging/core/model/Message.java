package com.jw.screw.logging.core.model;

import com.jw.screw.common.util.IdUtils;
import com.jw.screw.common.util.Remotes;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class Message implements Serializable {

    private String id;

    private Date createTime;

    private String host;

    /**
     * <b>日志信息来源</b>
     * <p>1.某个服务</p>
     * <p>2.slf4j传输</p>
     */
    private String source;

    /**
     * <b>传输类型</b>
     * <p>1.local</p>
     * <p>2.RPC</p>
     * <p>3.AMQP</p>
     * <p>4.kafka</p>
     * <p>5.es</p>
     * ....
     */
    private String transferType;

    /**
     * <b>日志类型</b>
     * <p>1.slf4j log日志输出</p>
     * <p>2.本地功能日志<，确认是什么功能/p>
     * <p>3.rpc远程日志</p>
     */
    private String type;

    /**
     * 内容
     */
    private String content;

    private String traceId;

    public Message() {
        this.id = String.valueOf(IdUtils.getNextId());
        this.createTime = new Date();
        this.host = Remotes.getHost();
    }
}
