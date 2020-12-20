package com.jw.screw.loadbalance;


import com.jw.screw.common.util.Collections;
import com.jw.screw.remote.netty.SConnector;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询算法
 * @author jiangw
 * @date 2020/12/3 16:57
 * @since 1.0
 */
public class RoundRobinRule extends AbstractRule {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public synchronized SConnector chose() {
        List<SConnector> connectors = getConnectors();
        if (Collections.isEmpty(connectors)) {
            return null;
        }
        int connectorSize = connectors.size();

        while (retryCount.get() < RETRY_NUMS) {
            if (counter.get() == connectorSize - 1) {
                counter.set(0);
            }
            SConnector connector = connectors.get(counter.get());
            if (connector.channelGroup().isAvailable()) {
                retryCount.set(0);
                return connector;
            }
        }

        retryCount.set(0);
        return null;
    }
}
