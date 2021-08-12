package com.jw.screw.logging.core.component;

import com.alibaba.fastjson.JSON;
import com.jw.screw.logging.core.constant.LogType;
import com.jw.screw.logging.core.model.Message;
import com.jw.screw.storage.Executor;
import com.jw.screw.storage.ExecutorHousekeeper;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

@Plugin(name = ScrewLog4j2.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class ScrewLog4j2 extends AbstractAppender implements Slf4jLogger {

    public static final String PLUGIN_NAME = "ScrewAppender";
    private Executor executor;
    private final String appName;
    private final boolean enable;

    protected ScrewLog4j2(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, String appName, boolean enable) throws SQLException, IOException, ClassNotFoundException {
        super(name, filter, layout, ignoreExceptions);
        this.appName = appName;
        executor = ExecutorHousekeeper.getExecutor();
        this.enable = enable;
    }

    @Override
    public void append(LogEvent event) {
        if (enable) {
            Message message = new Message();
            message.setSource(appName);
            message.setType(LogType.SLF4J_LOG);
            message.setContent(JSON.toJSONString(event));
            try {
                if (executor == null) {
                    executor = ExecutorHousekeeper.getExecutor();
                }
                send(message, this.executor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @PluginFactory
    public static ScrewLog4j2 createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("ignoreExceptions") boolean ignoreExceptions,
            @PluginElement("appName") String appName,
            @PluginElement("enable") boolean enable
    ) throws SQLException, IOException, ClassNotFoundException {
        return new ScrewLog4j2(name, filter, layout, ignoreExceptions, appName, enable);
    }
}
