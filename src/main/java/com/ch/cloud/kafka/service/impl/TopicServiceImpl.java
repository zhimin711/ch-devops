package com.ch.cloud.kafka.service.impl;

import com.ch.StatusS;
import com.ch.cloud.kafka.mapper.BtTopicMapper;
import com.ch.cloud.kafka.model.BtTopic;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.service.ITopicService;
import com.ch.mybatis.service.BaseService;
import com.ch.mybatis.utils.ExampleUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.ch.utils.SQLUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.Sqls;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 01370603
 * @date 2018/9/25 19:14
 */
@Service
public class TopicServiceImpl extends BaseService<Long, BtTopic> implements ITopicService {

    @Autowired(required = false)
    private BtTopicMapper topicMapper;

    @Override
    protected Mapper<BtTopic> getMapper() {
        return topicMapper;
    }

    @Override
    public BtTopic findByClusterAndTopic(String cluster, String topic) {
        BtTopic q = new BtTopic();
        q.setClusterName(cluster);
        q.setTopicName(topic);
        if (CommonUtils.isEmpty(cluster) || CommonUtils.isEmpty(cluster)) {
//            return null;
        }
        return getMapper().selectOne(q);
    }

    @Override
    public List<BtTopic> findByClusterLikeTopic(String clusterName, String topicName) {
        Sqls sqls = Sqls.custom().andEqualTo("clusterName", clusterName);
        if (CommonUtils.isNotEmpty(topicName)) {
            sqls.andLike("topicName", SQLUtils.likeAny(topicName));
        }
        Example example = Example.builder(BtTopic.class).andWhere(sqls).build();
        return getMapper().selectByExample(example);
    }

    @Override
    public int saveOrUpdate(List<TopicInfo> topicList, String clusterName, String username) {
        if (CommonUtils.isEmpty(topicList)) return 0;
        AtomicInteger c = new AtomicInteger();
        topicList.forEach(r -> {
            BtTopic topic = this.findByClusterAndTopic(clusterName, r.getName());
            if (topic != null) {
                topic.setPartitionSize(r.getPartitionSize());
                topic.setReplicaSize(r.getReplicaSize());
                topic.setStatus(StatusS.ENABLED);
                topic.setUpdateBy(username);
                topic.setUpdateAt(DateUtils.current());
                c.addAndGet(getMapper().updateByPrimaryKey(topic));
            } else {
                BtTopic topic1 = new BtTopic();
                topic1.setClusterName(clusterName);
                topic1.setTopicName(r.getName());
                topic1.setPartitionSize(r.getPartitionSize());
                topic1.setReplicaSize(r.getReplicaSize());
                topic1.setType("STRING");
                topic1.setStatus(StatusS.ENABLED);
                topic1.setCreateBy(username);
                topic1.setCreateAt(DateUtils.current());
                c.addAndGet(getMapper().insertSelective(topic1));
            }
        });
        return c.get();
    }

    @Override
    public PageInfo<BtTopic> findPage(BtTopic record, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Example ex = new Example(BtTopic.class);
        Example.Criteria criteria = ex.createCriteria();
        ExampleUtils.dynEqual(criteria, record, "clusterName");
        ExampleUtils.dynLike(criteria, record, "topicName");
        criteria.andNotEqualTo("status", StatusS.DELETE);
        ex.orderBy("clusterName").asc().orderBy("topicName").asc();
        List<BtTopic> records = getMapper().selectByExample(ex);
        return new PageInfo<>(records);
    }
}
