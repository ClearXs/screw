package com.jw.screw.spring.event;

import org.springframework.context.ApplicationEvent;

/**
 * screw
 * @author jiangw
 * @date 2020/12/10 17:34
 * @since 1.0
 */
public class PropertiesRefreshEvent extends ApplicationEvent {

    /**
     * Create a new ApplicationEvent.
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public PropertiesRefreshEvent(Object source) {
        super(source);
    }
}