package com.jw.screw.spring.config;

import com.jw.screw.common.event.Observable;
import com.jw.screw.common.event.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 可连接监听器
 * @author jiangw
 * @date 2021/4/16 15:23
 * @since 1.0
 */
public abstract class ConnectionListener implements Observer {

    private static Logger logger = LoggerFactory.getLogger(ConnectionListener.class);

    @Override
    public void update(Observable observable, Object... args) {
        Object arg = args[0];
        if (arg instanceof Exception) {
            Exception e = (Exception) arg;
            reject(observable, e);
        } else {
            accept(observable, arg);
        }
    }

    /**
     * 可连接时的回调
     * @param observable 被观测者
     * @param obj 连接成功后返回的对象
     */
    protected void accept(Observable observable, Object obj) {
        if (logger.isInfoEnabled()) {
            logger.info("connection acceptable, observable: [{}], obj: [{}]", observable, obj);
        }
    }

    /**
     * 连接拒绝的回调
     * @param observable 被观测者
     * @param e 异常
     */
    protected void reject(Observable observable, Exception e) {
        if (logger.isErrorEnabled()) {
            logger.error("connection reject, observable: [{}], obj: [{}]", observable, e.getMessage());
        }
    }
}
