package com.ch.cloud.rocketmq.manager.impl;

import com.ch.cloud.rocketmq.manager.RMQClusterManager;
import com.ch.cloud.rocketmq.util.JsonUtil;
import com.ch.cloud.rocketmq.util.RMQAdminUtil;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.KVTable;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Properties;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/16
 */
@Service
@Slf4j
public class RMQClusterManagerImpl implements RMQClusterManager {

    @SneakyThrows
    @Override
    public Map<String, Object> list() {
        Map<String, Object> resultMap = Maps.newHashMap();
        ClusterInfo clusterInfo = RMQAdminUtil.getClient().examineBrokerClusterInfo();
        log.info("op=look_clusterInfo {}", JsonUtil.obj2String(clusterInfo));
        Map<String/*brokerName*/, Map<Long/* brokerId */, Object/* brokerDetail */>> brokerServer = Maps.newHashMap();
        for (BrokerData brokerData : clusterInfo.getBrokerAddrTable().values()) {
            Map<Long, Object> brokerMasterSlaveMap = Maps.newHashMap();
            for (Map.Entry<Long/* brokerId */, String/* broker address */> brokerAddr : brokerData.getBrokerAddrs()
                    .entrySet()) {
                KVTable kvTable = RMQAdminUtil.getClient().fetchBrokerRuntimeStats(brokerAddr.getValue());
                //                KVTable kvTable = mqAdminExt.fetchBrokerRuntimeStats("127.0.0.1:10911");
                brokerMasterSlaveMap.put(brokerAddr.getKey(), kvTable.getTable());
            }
            brokerServer.put(brokerData.getBrokerName(), brokerMasterSlaveMap);
        }
        resultMap.put("clusterInfo", clusterInfo);
        resultMap.put("brokerServer", brokerServer);
        return resultMap;
    }

    @SneakyThrows
    @Override
    public Properties getBrokerConfig(String brokerAddr) {
        return RMQAdminUtil.getClient().getBrokerConfig(brokerAddr);
    }
}
