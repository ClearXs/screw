package com.jw.screw.common;

import com.jw.screw.common.event.ConnectionEvent;
import com.jw.screw.common.event.Observer;
import com.jw.screw.common.exception.ConnectionException;

/**
 *
 * @author jiangw
 * @date 2021/4/15 17:57
 * @since 1.0
 */
public abstract class ConnectionCallback implements Callback {

    private final ConnectionEvent event = new ConnectionEvent();

    public ConnectionCallback(Observer observer) {
        event.attach(observer);
    }

    @Override
    public void acceptable(Object accept) throws ConnectionException {
        event.accept(accept);
    }

    @Override
    public void rejected(Exception e) {
        event.reject(e);
    }
}
