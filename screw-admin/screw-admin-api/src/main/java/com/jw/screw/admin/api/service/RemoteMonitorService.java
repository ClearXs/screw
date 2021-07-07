package com.jw.screw.admin.api.service;

import cn.hutool.core.date.DateUtil;
import com.jw.screw.admin.api.model.ServerMonitorModel;
import com.jw.screw.admin.api.model.ServerMonitorQueryDTO;
import com.jw.screw.admin.common.constant.StringPool;
import com.zzht.patrol.monitor.core.MonitorModel;
import com.zzht.patrol.monitor.core.mircometer.Metrics;
import com.zzht.patrol.screw.common.SystemConfig;
import com.zzht.patrol.screw.common.model.Tuple;
import com.zzht.patrol.screw.common.transport.RemoteAddress;
import com.zzht.patrol.screw.monitor.remote.MonitorProvider;
import com.zzht.patrol.screw.monitor.remote.TracingModel;
import com.zzht.patrol.screw.remote.netty.config.GlobeConfig;
import com.zzht.patrol.screw.spring.ScrewSpringMonitor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @see MonitorProvider
 * @author jiangw
 * @date 2020/12/28 9:34
 * @since 1.0
 */
@Service
public class RemoteMonitorService implements InitializingBean {

    @Autowired
    private ScrewSpringMonitor monitor;

    private MonitorProvider monitorProvider;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition waitCondition = lock.newCondition();

    final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    public void afterPropertiesSet() throws Exception {
        monitorProvider = monitor.getMonitorProvider();
    }

    public Set<ServerMonitorModel> getServerMonitorList(String serverKey) {
        Set<ServerMonitorModel> monitorModels = new HashSet<>();
        Map<MonitorModel, String> serverHealth = monitorProvider.getServerHealthByKey(serverKey);
        if (CollectionUtils.isEmpty(serverHealth)) {
            return monitorModels;
        }
        for (Map.Entry<MonitorModel, String> entry : serverHealth.entrySet()) {
            MonitorModel monitorModel = entry.getKey();
            // 如果 host port一致的两个monitor model，在构建时只构建一个。
            ServerMonitorModel serverMonitorModel = null;
            if (CollectionUtils.isEmpty(monitorModels)) {
                serverMonitorModel = new ServerMonitorModel();
            } else {
                for (ServerMonitorModel model : monitorModels) {
                    if (model.equals(monitorModel.getAddress().getHost(), monitorModel.getAddress().getPort())) {
                        serverMonitorModel = model;
                    }
                }
                if (serverMonitorModel == null) {
                    serverMonitorModel = new ServerMonitorModel();
                }
            }
            if (SystemConfig.PROVIDER.getRole().equals(monitorModel.getRole())) {
                serverMonitorModel.setProviderKey(monitorModel.getServerKey());
                serverMonitorModel.setProviderRole(SystemConfig.PROVIDER.getRole());
            } else if (SystemConfig.CONSUMER.getRole().equals(monitorModel.getRole())) {
                serverMonitorModel.setConsumerKey(monitorModel.getServerKey());
                serverMonitorModel.setConsumerRole(SystemConfig.CONSUMER.getRole());
            }
            serverMonitorModel.setHost(monitorModel.getAddress().getHost());
            serverMonitorModel.setPort(monitorModel.getAddress().getPort());
            serverMonitorModel.setLastUpdateTime(DateUtil.formatDateTime(new Date(monitorModel.getLastUpdateTime())));
            serverMonitorModel.setHealth(entry.getValue());
            monitorModels.add(serverMonitorModel);
        }
        return monitorModels;
    }

    public ServerMonitorModel getServerMonitorMetrics(ServerMonitorQueryDTO serverQuery) throws InterruptedException, ExecutionException, TimeoutException {
        lock.lock();
        try {
            final AtomicReference<Tuple<MonitorModel, Map<String, List<Metrics>>>> owner = new AtomicReference<>();
            monitorProvider.activeMetricsCollect(serverQuery.getServerKey(), new RemoteAddress(serverQuery.getServerHost(), serverQuery.getServerPort()));
            ServerMonitorModel serverMonitorModel = new ServerMonitorModel();
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    lock.lock();
                    try {
                        // 自旋
                        while (owner.compareAndSet(null, monitorProvider.getServerMetrics())) {
                            owner.set(monitorProvider.getServerMetrics());
                        }
                        waitCondition.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            });
            while ((waitCondition.awaitNanos(TimeUnit.MILLISECONDS.toNanos(GlobeConfig.CONNECT_TIMEOUT_MILLIS))) <= 0) {
                stopCollect();
                throw new NullPointerException();
            }
            Tuple<MonitorModel, Map<String, List<Metrics>>> serverMetrics = owner.get();
            MonitorModel monitorModel = serverMetrics.getKey();
            if (SystemConfig.CONSUMER.getRole().equals(monitorModel.getRole())) {
                serverMonitorModel.setConsumerKey(monitorModel.getServerKey());
                serverMonitorModel.setConsumerRole(SystemConfig.CONSUMER.getRole());
            } else if (SystemConfig.PROVIDER.getRole().equals(monitorModel.getRole())) {
                serverMonitorModel.setProviderKey(monitorModel.getServerKey());
                serverMonitorModel.setConsumerRole(SystemConfig.PROVIDER.getRole());
            }
            serverMonitorModel.setHost(monitorModel.getAddress().getHost());
            serverMonitorModel.setPort(monitorModel.getAddress().getPort());
            serverMonitorModel.setMetrics(serverMetrics.getValue());
            stopCollect();
            return serverMonitorModel;
        } finally {
            lock.unlock();
        }
    }

    private void stopCollect() {
        monitorProvider.stopMetricsCollect();
    }

    public TracingModel getServerTracing(ServerMonitorQueryDTO serverQuery) {
        ConcurrentHashMap<String, TracingModel> serverTracing = monitorProvider.getServerTracing();
        return serverTracing.get(serverQuery.getServerKey() + StringPool.AT + serverQuery.getServerHost() + StringPool.AT + serverQuery.getServerPort());
    }
}
