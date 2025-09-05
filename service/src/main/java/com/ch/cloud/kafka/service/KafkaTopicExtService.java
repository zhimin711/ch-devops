package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.KafkaTopicExt;
import com.ch.cloud.kafka.model.KafkaTopicExtProp;
import com.ch.mybatis.service.IService;

import java.util.List;

/**
 * @author zhimin.ma
 * @since 2018/9/25 18:20
 */
public interface KafkaTopicExtService extends IService<KafkaTopicExt> {

    List<KafkaTopicExt> findByClusterIdAndTopicNameAndCreateBy(Long clusterId, String topicName, String username);

    List<KafkaTopicExtProp> findProps(Long id);
}
