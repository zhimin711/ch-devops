package com.ch.cloud.rocketmq.manager.impl;

import com.ch.cloud.rocketmq.util.RMQAdminUtil;
import com.ch.cloud.rocketmq.manager.RMQProducerManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.protocol.body.ProducerConnection;
import org.springframework.stereotype.Service;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/16
 */
@Service
@Slf4j
public class RMQProducerManagerImpl implements RMQProducerManager {
    
    @Override
    public ProducerConnection getProducerConnection(String producerGroup, String topic) throws Exception {
        return RMQAdminUtil.getClient().examineProducerConnectionInfo(producerGroup, topic);
    }
    
}
