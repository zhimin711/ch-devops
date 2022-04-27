package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.BtTopic;
import com.ch.cloud.kafka.pojo.TopicDto;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.mybatis.service.IService;

import java.util.List;

/**
 * @author zhimin.ma
 * @date 2018/9/25 18:20
 */
public interface ITopicService extends IService<BtTopic> {

    BtTopic findByClusterAndTopic(String cluster, String topic);

    List<BtTopic> findByClusterLikeTopic(String clusterName, String topicName);

    int saveOrUpdate(List<TopicInfo> topicList, String clusterName, String username);

    TopicDto check(String cluster, String topic);

    int update(BtTopic srcRecord, BtTopic targetRecord);
}
