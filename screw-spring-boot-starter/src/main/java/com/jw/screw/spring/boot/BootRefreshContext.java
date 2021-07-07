package com.jw.screw.spring.boot;

import com.jw.screw.spring.config.ValueRefreshContext;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * spring boot value refresh类，
 * @author jiangw
 * @date 2021/1/13 17:14
 * @since 1.0
 */
@Component("bootRefreshContext")
public class BootRefreshContext extends ValueRefreshContext {

    @Override
    public void onPut(ApplicationContext applicationContext) {
        super.onPut(applicationContext);
        put(ConfigurationPropertiesRefresh.getInstance(applicationContext));
    }
}
