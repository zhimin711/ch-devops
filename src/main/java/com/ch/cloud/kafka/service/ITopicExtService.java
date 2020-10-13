package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.mybatis.service.IService;

/**
 * @author zhimin.ma
 * @date 2018/9/25 18:20
 */
public interface ITopicExtService extends IService<Long, BtTopicExt> {

    BtTopicExt findByClusterAndTopicAndCreateBy(String clusterName, String topicName, String username);
}
