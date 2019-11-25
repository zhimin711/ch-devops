package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.BtTopic;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.mybatis.service.IService;

import java.util.List;

/**
 * @author 01370603
 * @date 2018/9/25 18:20
 */
public interface ITopicService extends IService<Long, BtTopic> {

    BtTopic findByClusterAndTopic(String cluster, String topic);

    List<BtTopic> findByClusterLikeTopic(String clusterName, String topicName);

    int saveOrUpdate(List<TopicInfo> topicList, String clusterName, String username);
}
