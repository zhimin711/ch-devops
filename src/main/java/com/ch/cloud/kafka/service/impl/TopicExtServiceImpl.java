package com.ch.cloud.kafka.service.impl;

import com.ch.cloud.kafka.mapper.BtTopicExtMapper;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.service.ITopicExtService;
import com.ch.mybatis.service.BaseService;
import com.ch.utils.CommonUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;

/**
 * @author zhimin.ma
 * @date 2018/9/25 19:14
 */
@Service
public class TopicExtServiceImpl extends BaseService<Long, BtTopicExt> implements ITopicExtService {

    @Resource
    private BtTopicExtMapper mapper;

    @Override
    protected Mapper<BtTopicExt> getMapper() {
        return mapper;
    }

    @Override
    public BtTopicExt findByClusterAndTopicAndCreateBy(String clusterName, String topicName, String username) {
        if (CommonUtils.isEmptyOr(clusterName, topicName, username)) {
            return null;
        }
        BtTopicExt q = new BtTopicExt();
        q.setClusterName(clusterName);
        q.setTopicName(topicName);
        q.setCreateBy(username);
        return getMapper().selectOne(q);
    }

}
