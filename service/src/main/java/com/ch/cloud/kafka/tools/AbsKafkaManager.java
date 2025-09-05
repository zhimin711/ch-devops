package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.e.PubError;
import com.ch.utils.AssertUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/6/5 20:32
 */
public abstract class AbsKafkaManager {

    @Autowired
    protected KafkaClusterService kafkaClusterService;

    public AdminClient getAdminClient(Long clusterId) {
        KafkaCluster config = kafkaClusterService.find(clusterId);
        AssertUtils.isEmpty(config, PubError.NOT_EXISTS, "cluster id" + clusterId);
        return KafkaClusterUtils.getAdminClient(config);
    }
}
