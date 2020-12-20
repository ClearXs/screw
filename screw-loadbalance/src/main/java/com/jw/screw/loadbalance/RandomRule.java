package com.jw.screw.loadbalance;


import com.jw.screw.common.util.Collections;
import com.jw.screw.remote.netty.SConnector;

import java.util.List;

/**
 * 随机算法
 * @author jiangw
 * @date 2020/12/3 16:42
 * @since 1.0
 */
public class RandomRule extends AbstractRule {

    @Override
    public synchronized SConnector chose() {
        List<SConnector> connectors = getConnectors();
        if (Collections.isEmpty(connectors)) {
            return null;
        }
        int connectorSize = connectors.size();
        while (retryCount.getAndIncrement() < RETRY_NUMS) {
            SConnector connector = connectors.get(randomInt(connectorSize));
            if (connector.channelGroup().isAvailable()) {
                retryCount.set(0);
                return connector;
            }
        }
        retryCount.set(0);
        return null;
    }
}
