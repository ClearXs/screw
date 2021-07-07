package com.jw.screw.spring.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

/**
 * 连接拒绝的事件
 * @author jiangw
 * @date 2021/4/16 16:05
 * @since 1.0
 */
public class ConnectionRejectEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public ConnectionRejectEvent(ApplicationContext source) {
        super(source);
    }


}
