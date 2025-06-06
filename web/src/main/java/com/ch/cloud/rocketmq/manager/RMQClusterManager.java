package com.ch.cloud.rocketmq.manager;

import com.ch.cloud.rocketmq.config.RMQConfigure;

import java.util.List;
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
