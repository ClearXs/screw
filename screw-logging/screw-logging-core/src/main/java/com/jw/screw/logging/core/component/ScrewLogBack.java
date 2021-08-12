package com.jw.screw.logging.core.component;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.alibaba.fastjson.JSON;
import com.jw.screw.logging.core.constant.LogType;
import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.Executor;
import com.jw.screw.storage.ExecutorHousekeeper;

public class ScrewLogBack extends AppenderBase<LoggingEvent> implements Slf4jLogger {

    private Executor executor;
    private String appName;
    private boolean enable;

    public ScrewLogBack() {
        executor = ExecutorHousekeeper.getExecutor();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    protected void append(LoggingEvent eventObject) {
        if (isEnable()) {
            Message message = new Message();
            message.setSource(getAppName());
            message.setType(LogType.SLF4J_LOG);
            message.setContent(JSON.toJSONString(eventObject));
            try {
                if (executor == null) {
                    executor = ExecutorHousekeeper.getExecutor();
                }
                send(message, executor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
