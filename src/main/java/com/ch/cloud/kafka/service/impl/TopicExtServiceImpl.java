package com.ch.cloud.kafka.service.impl;

import com.ch.cloud.kafka.mapper.BtTopicExtMapper;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.service.TopicExtService;
import com.ch.mybatis.service.BaseService;
import com.ch.utils.CommonUtils;
import com.ch.utils.SQLUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.Sqls;

import java.util.List;

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

    @Override
    public List<BtTopicExt> findByClusterLikeTopic(String clusterName, String topicName) {
        Sqls sqls = Sqls.custom().andEqualTo("clusterName", clusterName);
        if (CommonUtils.isNotEmpty(topicName)) {
            sqls.andLike("topicName", SQLUtils.likeAny(topicName));
        }
        Example example = Example.builder(BtTopicExt.class).andWhere(sqls).build();
        return topicExtMapper.selectByExample(example);
    }
}
