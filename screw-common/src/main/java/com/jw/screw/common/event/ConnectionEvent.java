package com.jw.screw.common.event;

/**
 * 连接发生的事件
 * @author jiangw
 * @date 2021/4/16 15:09
 * @since 1.0
 */
public class ConnectionEvent extends ScrewEvent {

    public void accept(Object accept) {
        notifyObservers(accept);
    }

    public void reject(Exception e) {
        notifyObservers(e);
    }
}
