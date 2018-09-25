package com.ch.cloud.kafka.service.impl;

import com.ch.cloud.kafka.mapper.BtTopicExtMapper;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.service.TopicExtService;
import com.ch.mybatis.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author 01370603
 * @date 2018/9/25 19:14
 */
@Service
public class TopicExtServiceImpl extends BaseService<Long, BtTopicExt> implements TopicExtService {

    @Autowired(required = false)
    private BtTopicExtMapper topicExtMapper;

    @Override
    protected Mapper<BtTopicExt> getMapper() {
        return topicExtMapper;
    }

    @Override
    public BtTopicExt findByClusterAndTopic(String cluster, String topic) {
        BtTopicExt q = new BtTopicExt();
        q.setClusterName(cluster);
        q.setTopicName(topic);
        return topicExtMapper.selectOne(q);
    }
}
