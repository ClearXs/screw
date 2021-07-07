package com.jw.screw.loadbalance;

import com.jw.screw.common.util.Collections;
import com.jw.screw.remote.netty.SConnector;

import java.util.List;
import java.util.PriorityQueue;

/**
 * 加权随机
 * @author jiangw
 * @date 2020/12/3 18:00
 * @since 1.0
 */
public class WeightRandomRule extends AbstractRule {

    @Override
    public SConnector chose() {
        List<SConnector> connectors = getConnectors();
        if (Collections.isEmpty(connectors)) {
            return null;
        }
        // 统计权重和
        int weightSum = 0;
        for (SConnector connector : connectors) {
            weightSum += connector.weight();
        }
        int randomWeight = randomInt(weightSum);
        // 根据权重排序connectors
        PriorityQueue<SConnector> sortedConnectors = new PriorityQueue<>(connectors);
        int traverseWeightSum = 0;
        for (SConnector sortedConnector : sortedConnectors) {
            if (sortedConnector.channelGroup().isAvailable()) {
                traverseWeightSum += sortedConnector.weight();
            }
            if (traverseWeightSum >= randomWeight) {
                return sortedConnector;
            }
        }
        return null;
    }
}
