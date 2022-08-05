package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.dto.KafkaTopicDTO;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.mybatis.service.IService;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * @author zhimin.ma
 * @since 2018/9/25 18:20
 */
public interface KafkaTopicService extends IService<KafkaTopic> {

    KafkaTopic findByClusterIdAndTopicName(Long clusterId, String topicName);
    
    /**
     * 查询集群主题
     * @param clusterId 集群ID
     * @param topicName 主题名称（非必须）
     * @return
     */
    List<KafkaTopic> findByClusterIdLikeTopicName(@NotNull Long clusterId, String topicName);

    int saveOrUpdate(List<KafkaTopicDTO> topicList, String username);

    KafkaTopicDTO check(Long clusterId, String topic);
    KafkaTopic check(Long clusterId, Long topicId);

    int update(KafkaTopic srcRecord, KafkaTopic targetRecord);

    List<KafkaTopic> findByClusterIdAndTopicNames(Long id, Set<String> topicNames);

}
