package com.ch.cloud.kafka.service.impl;

import com.ch.Constants;
import com.ch.StatusS;
import com.ch.cloud.kafka.mapper.KafkaTopicMapper;
import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.dto.KafkaTopicDTO;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.mybatis.service.ServiceImpl;
import com.ch.mybatis.utils.ExampleUtils;
import com.ch.utils.AssertUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.ch.utils.SQLUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.Sqls;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhimin.ma
 * @since 2018/9/25 19:14
 */
@Service
public class KafkaTopicServiceImpl extends ServiceImpl<KafkaTopicMapper, KafkaTopic> implements KafkaTopicService {

    @Autowired
    private KafkaClusterService kafkaClusterService;

    @Value("${fs.path.libs}")
    private String libsDir;

    @Override
    public KafkaTopic findByClusterIdAndTopicName(Long clusterId, String topicName) {
        KafkaTopic q = new KafkaTopic();
        q.setClusterId(clusterId);
        q.setTopicName(topicName);
        if (CommonUtils.isEmpty(clusterId)) {
            // return null;
        }
        return getMapper().selectOne(q);
    }

    @Override
    public List<KafkaTopic> findByClusterIdLikeTopicName(Long clusterId, String topicName) {
        Sqls sqls = Sqls.custom().andEqualTo("clusterId", clusterId).andEqualTo("status", Constants.ENABLED);
        if (CommonUtils.isNotEmpty(topicName)) {
            sqls.andLike("topicName", SQLUtils.likeAny(topicName));
        }
        Example example = Example.builder(KafkaTopic.class).andWhere(sqls).build();
        return getMapper().selectByExample(example);
    }

    @Override
    public int saveOrUpdate(List<KafkaTopicDTO> topicList, String username) {
        if (CommonUtils.isEmpty(topicList))
            return 0;
        AtomicInteger c = new AtomicInteger();
        topicList.forEach(r -> {
            KafkaTopic topic = this.findByClusterIdAndTopicName(r.getClusterId(), r.getTopicName());
            if (topic != null) {
                topic.setPartitionSize(r.getPartitionSize());
                topic.setReplicaSize(r.getReplicaSize());
                topic.setStatus(StatusS.ENABLED);
                topic.setUpdateBy(username);
                topic.setUpdateAt(DateUtils.current());
                c.addAndGet(getMapper().updateByPrimaryKey(topic));
            } else {
                KafkaTopic topic1 = new KafkaTopic();
                topic1.setClusterId(r.getClusterId());
                topic1.setTopicName(r.getTopicName());
                topic1.setPartitionSize(r.getPartitionSize());
                topic1.setReplicaSize(r.getReplicaSize());
                topic1.setType("JSON");
                topic1.setStatus(StatusS.ENABLED);
                topic1.setCreateBy(username);
                topic1.setCreateAt(DateUtils.current());
                c.addAndGet(getMapper().insertSelective(topic1));
            }
        });
        return c.get();
    }

    @Override
    public PageInfo<KafkaTopic> findPage(KafkaTopic record, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Example ex = new Example(KafkaTopic.class);
        Example.Criteria criteria = ex.createCriteria();
        ExampleUtils.dynEqual(criteria, record, "clusterId");
        ExampleUtils.dynLike(criteria, record, "topicName");
        criteria.andNotEqualTo("status", StatusS.DELETE);
        ex.orderBy("clusterId").asc().orderBy("topicName").asc();
        List<KafkaTopic> records = getMapper().selectByExample(ex);
        return new PageInfo<>(records);
    }

    @Override
    public KafkaTopicDTO check(Long clusterId, String topic) {
        KafkaCluster config = kafkaClusterService.find(clusterId);
        AssertUtils.isNull(config, PubError.NOT_EXISTS, clusterId + "集群配置");
        KafkaTopic kafkaTopic = this.findByClusterIdAndTopicName(clusterId, topic);
        AssertUtils.isNull(kafkaTopic, PubError.NOT_EXISTS, clusterId + ":" + topic + "主题配置");
        KafkaTopicDTO dto = new KafkaTopicDTO();
        BeanUtils.copyProperties(kafkaTopic, dto);
        dto.setZookeeper(config.getZookeeper());
        String path = libsDir + File.separator + kafkaTopic.getClassFile();
        dto.setClassFile(path);
        return dto;
    }

    @Override
    public KafkaTopic check(Long clusterId, Long topicId) {
        KafkaTopic kafkaTopic = this.find(topicId);
        AssertUtils.isNull(kafkaTopic, PubError.NOT_EXISTS, topicId + "主题配置");
        AssertUtils.isFalse(CommonUtils.isEquals(kafkaTopic.getClusterId(), clusterId), PubError.NOT_ALLOWED,
                clusterId + ":" + topicId + "主题配置");
        String path = libsDir + File.separator + kafkaTopic.getClassFile();
        kafkaTopic.setClassFile(path);
        return kafkaTopic;
    }

    @Override
    public int update(KafkaTopic srcRecord, KafkaTopic targetRecord) {
        Example example = ExampleUtils.create(srcRecord);

        return getMapper().updateByExampleSelective(targetRecord, example);
    }

    @Override
    public List<KafkaTopic> findByClusterIdAndTopicNames(Long id, Set<String> topicNames) {
        Example ex = Example.builder(KafkaTopic.class)
                .andWhere(Sqls.custom().andEqualTo("clusterId", id)
                        .andIn("topicName", topicNames)).build();
        return getMapper().selectByExample(ex);
    }
}
