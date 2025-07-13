package com.ch.cloud.rocketmq.manager;

import java.util.Map;
import java.util.Properties;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/16
 */
public interface RMQClusterManager {

    Map<String, Object> list();

    Properties getBrokerConfig(String brokerAddr);
}
