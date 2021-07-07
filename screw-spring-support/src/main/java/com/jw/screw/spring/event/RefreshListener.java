package com.jw.screw.spring.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 刷新事件的监听
 * @author jiangw
 * @date 2020/12/9 17:24
 * @since 1.0
 */
public class RefreshListener implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        ApplicationContext applicationContext = null;
        if (event instanceof ContextRefreshedEvent) {
            applicationContext = ((ContextRefreshedEvent) event).getApplicationContext();
        } else if (event instanceof PropertiesRefreshEvent) {
            applicationContext = (ApplicationContext) event.getSource();
        }
        if (applicationContext != null) {
            ConfigListener.refreshConfig(applicationContext);
        }
    }
}
