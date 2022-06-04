package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.dto.KafkaTopicDTO;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.mybatis.service.IService;

import java.util.List;

/**
 * @author zhimin.ma
 * @since 2018/9/25 18:20
 */
public interface KafkaTopicService extends IService<KafkaTopic> {

    KafkaTopic findByClusterIdAndTopicName(Long clusterId, String topicName);

    List<KafkaTopic> findByClusterIdLikeTopicName(Long clusterId, String topicName);

    int saveOrUpdate(List<TopicInfo> topicList, Long clusterId, String username);

    KafkaTopicDTO check(Long clusterId, String topic);

    int update(KafkaTopic srcRecord, KafkaTopic targetRecord);
}
