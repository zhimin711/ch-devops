package com.ch.cloud.kafka.service.impl;

import com.ch.Constants;
import com.ch.StatusS;
import com.ch.cloud.kafka.mapper.BtTopicMapper;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.model.BtTopic;
import com.ch.cloud.kafka.dto.TopicDTO;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.ITopicService;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.mybatis.service.ServiceImpl;
import com.ch.mybatis.utils.ExampleUtils;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhimin.ma
 * @since 2018/9/25 19:14
 */
@Service
public class TopicServiceImpl extends ServiceImpl<BtTopicMapper, BtTopic> implements ITopicService {

    @Autowired
    private ClusterConfigService clusterConfigService;

    @Value("${fs.path.libs}")
    private String libsDir;

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
        Sqls sqls = Sqls.custom().andEqualTo("clusterName", clusterName).andEqualTo("status", Constants.ENABLED);
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

    @Override
    public TopicDTO check(String cluster, String topic) {
        BtClusterConfig config = clusterConfigService.findByClusterName(cluster);
        if (config == null) {
            throw ExceptionUtils.create(PubError.NOT_EXISTS, cluster + "集群配置不存在!");
        }
        BtTopic topicExt = this.findByClusterAndTopic(cluster, topic);
        if (topicExt == null) {
            throw ExceptionUtils.create(PubError.NOT_EXISTS, cluster + ":" + topic + "主题配置不存在！");
        }
        TopicDTO dto = new TopicDTO();
        BeanUtils.copyProperties(topicExt, dto);
        dto.setZookeeper(config.getZookeeper());
        String path = libsDir + File.separator + topicExt.getClassFile();
        dto.setClassFile(path);
        return dto;
    }

    @Override
    public int update(BtTopic srcRecord, BtTopic targetRecord) {
        Example example = ExampleUtils.create(srcRecord);

        return getMapper().updateByExampleSelective(targetRecord, example);
    }
}
