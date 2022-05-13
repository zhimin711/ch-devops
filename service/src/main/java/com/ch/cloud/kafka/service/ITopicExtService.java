package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.model.BtTopicExtProp;
import com.ch.mybatis.service.IService;

import java.util.List;

/**
 * @author zhimin.ma
 * @date 2018/9/25 18:20
 */
public interface ITopicExtService extends IService<BtTopicExt> {

    List<BtTopicExt> findByClusterAndTopicAndCreateBy(String clusterName, String topicName, String username);

    List<BtTopicExtProp> findProps(Long id);
}
