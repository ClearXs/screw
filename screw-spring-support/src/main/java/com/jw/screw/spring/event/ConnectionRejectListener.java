package com.jw.screw.spring.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

/**
 * 取本地配置文件，读取成功之后同步spring缓存中的数据
 * @author jiangw
 * @date 2021/4/16 16:08
 * @since 1.0
 */
public class ConnectionRejectListener implements ApplicationListener<ConnectionRejectEvent> {

    @Override
    public void onApplicationEvent(ConnectionRejectEvent event) {
        ApplicationContext applicationContext = (ApplicationContext) event.getSource();
        ConfigListener.rejectConnection(applicationContext);
        applicationContext.publishEvent(new PropertiesRefreshEvent(applicationContext));
    }
}
