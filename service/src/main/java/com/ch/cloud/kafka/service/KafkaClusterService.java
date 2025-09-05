package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.mybatis.service.IService;

import java.util.List;

/**
 * @author zhimin.ma
 * @since 2018/9/25 18:20
 */
public interface KafkaClusterService extends IService<KafkaCluster> {

    KafkaCluster findByClusterName(String cluster);

    List<KafkaCluster> findEnabled();
}
