package com.jw.screw.spring.config;

import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个刷新spring配置的上下文，在Consumer启动时提供一个外部接口
 * @author jiangw
 * @date 2021/1/9 15:08
 * @since 1.0
 */
public class ValueRefreshContext {

    private final List<ValueRefresh> refreshList = new ArrayList<>();

    public synchronized void put(ValueRefresh valueRefresh) {
        if (!refreshList.contains(valueRefresh)) {
            refreshList.add(valueRefresh);
        }
    }

    public synchronized List<ValueRefresh> list() {
        return refreshList;
    }

    public void onPut(ApplicationContext applicationContext) {
        put(SpringValueRefresh.getInstance(applicationContext));
    }
}
