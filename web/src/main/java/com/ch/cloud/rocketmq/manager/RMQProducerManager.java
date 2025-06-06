package com.ch.cloud.rocketmq.manager;

import org.apache.rocketmq.common.protocol.body.ProducerConnection;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/16
 */
public interface RMQProducerManager {
    
    
    ProducerConnection getProducerConnection(String producerGroup, String topic) throws Exception;
    
}
