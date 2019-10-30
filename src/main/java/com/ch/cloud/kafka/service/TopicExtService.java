package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.mybatis.service.IService;

import java.util.List;

/**
 * @author 01370603
 * @date 2018/9/25 18:20
 */
public interface TopicExtService extends IService<Long, BtTopicExt> {

    BtTopicExt findByClusterAndTopic(String cluster, String topic);

    List<BtTopicExt> findByClusterLikeTopic(String clusterName, String topicName);
}
