package com.ch.cloud.kafka.controller;

import com.ch.StatusS;
import com.ch.cloud.kafka.dto.BrokerDTO;
import com.ch.cloud.kafka.dto.ConsumerGroupDTO;
import com.ch.cloud.kafka.dto.KafkaTopicConfigDTO;
import com.ch.cloud.kafka.dto.KafkaTopicDTO;
import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.pojo.Partition;
import com.ch.cloud.kafka.pojo.TopicConfig;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.cloud.kafka.tools.*;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.result.InvokerPage;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.AssertUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author zhimin.ma
 * @since 2018/9/25 20:29
 */
@Api(tags = "KAFKA主题配置模块")
@RestController
@RequestMapping("/kafka/topic")
public class KafkaTopicController {

    @Autowired
    private KafkaTopicService kafkaTopicService;
    @Autowired
    private KafkaClusterService kafkaClusterService;
    @Autowired
    private KafkaClusterManager kafkaClusterManager;
    @Autowired
    private KafkaConsumerGroupManager kafkaConsumerGroupManager;
    @Autowired
    private KafkaTopicManager kafkaTopicManager;

    @ApiOperation(value = "分页查询", notes = "需要在请求头中附带token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "num", value = "页码", required = true),
            @ApiImplicitParam(name = "size", value = "分页大小", required = true),
            @ApiImplicitParam(name = "record", value = "查询条件", paramType = "query")
    })
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<KafkaTopic> page(KafkaTopic record,
                                       @PathVariable(value = "num") int pageNum,
                                       @PathVariable(value = "size") int pageSize) {
        return ResultUtils.wrapPage(() -> {
            AssertUtils.isEmpty(record.getClusterId(), PubError.NON_NULL, "集群ID");
            PageInfo<KafkaTopic> pageInfo = kafkaTopicService.findPage(record, pageNum, pageSize);
            return InvokerPage.build(pageInfo.getTotal(), pageInfo.getList());
        });

    }

    @ApiOperation(value = "新增主题信息", notes = "")
    @PostMapping
    public Result<Integer> add(@RequestBody KafkaTopic record) {
        return ResultUtils.wrapFail(() -> {
            AssertUtils.isEmpty(record.getClusterId(), PubError.NON_NULL, "集群ID");
            KafkaTopic r = kafkaTopicService.findByClusterIdAndTopicName(record.getClusterId(), record.getTopicName());

            AssertUtils.isTrue(r != null && !CommonUtils.isEquals(r.getStatus(), StatusS.DELETE), PubError.EXISTS, "主题已存在！");
            KafkaCluster cluster = kafkaClusterService.find(record.getClusterId());


            Set<String> topicNames = KafkaClusterUtils.fetchTopicNames(cluster);

            if (!topicNames.contains(record.getTopicName())) {
                kafkaTopicManager.createTopic(record);
            } else {
                TopicInfo info = kafkaClusterManager.info(cluster.getId(), record.getTopicName());
                record.setPartitionSize(info.getPartitionSize());
                record.setReplicaSize(info.getReplicaSize());
            }

            if (r != null) { // 已删除主题重建
                r.setPartitionSize(record.getPartitionSize());
                r.setReplicaSize(record.getReplicaSize());
                r.setType(record.getType());
                r.setClassFile(record.getClassFile());
                r.setClusterId(record.getClusterId());
                r.setDescription(record.getDescription());
                r.setStatus(StatusS.ENABLED);
                r.setUpdateBy(ContextUtil.getUsername());
                r.setUpdateAt(DateUtils.current());
                return kafkaTopicService.update(r);
            }
            record.setCreateBy(ContextUtil.getUsername());
            record.setStatus(StatusS.ENABLED);
            return kafkaTopicService.save(record);
        });
    }

    private void createTopic(KafkaCluster cluster, KafkaTopic record) {
        TopicConfig config = new TopicConfig();
        config.setZookeeper(cluster.getZookeeper());
        config.setTopicName(record.getTopicName());
        config.setPartitions(record.getPartitionSize());
        config.setReplicationFactor(record.getReplicaSize());
//        TopicManager.createTopic(config);
        ZkTopicUtils.createTopicByCommand(config);
    }

    @ApiOperation(value = "修改主题信息", notes = "")
    @PutMapping({"{id:[0-9]+}"})
    public Result<Integer> update(@PathVariable Long id, @RequestBody KafkaTopic record) {
        return ResultUtils.wrapFail(() -> {
            AssertUtils.isEmpty(record.getClusterId(), PubError.NON_NULL, "集群ID");
            record.setUpdateBy(ContextUtil.getUsername());
            record.setUpdateAt(DateUtils.current());
            return kafkaTopicService.update(record);
        });
    }

    @ApiOperation(value = "获取主题信息", notes = "")
    @GetMapping({"{id:[0-9]+}"})
    public Result<TopicInfo> detail(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            KafkaTopic record = kafkaTopicService.find(id);
            AssertUtils.isEmpty(record, PubError.NOT_EXISTS, "主题ID");
            return kafkaClusterManager.info(record.getClusterId(), record.getTopicName());
        });
    }

    @ApiOperation(value = "获取主题信息", notes = "")
    @GetMapping({"{id:[0-9]+}/partitions"})
    public Result<Partition> detailPartitions(@PathVariable Long id) {
        return ResultUtils.wrap(() -> {
            KafkaTopic record = kafkaTopicService.find(id);
            AssertUtils.isEmpty(record, PubError.NOT_EXISTS, "主题ID");
            return kafkaClusterManager.partitions(record.getClusterId(), record.getTopicName());
        });
    }

    @ApiOperation(value = "获取主题信息", notes = "")
    @GetMapping({"{id:[0-9]+}/brokers"})
    public Result<BrokerDTO> detailBrokers(@PathVariable Long id) {
        return ResultUtils.wrap(() -> {
            KafkaTopic record = kafkaTopicService.find(id);
            AssertUtils.isEmpty(record, PubError.NOT_EXISTS, "主题ID");
            return kafkaClusterManager.brokers(record.getClusterId(), record.getTopicName());
        });
    }

    @ApiOperation(value = "获取主题信息", notes = "")
    @GetMapping({"{id:[0-9]+}/consumerGroups"})
    public Result<ConsumerGroupDTO> detailConsumerGroups(@PathVariable Long id) {
        return ResultUtils.wrap(() -> {
            KafkaTopic record = kafkaTopicService.find(id);
            AssertUtils.isEmpty(record, PubError.NOT_EXISTS, "主题ID");
            return kafkaConsumerGroupManager.consumerGroups(record.getClusterId(), record.getTopicName());
        });
    }

    @ApiOperation(value = "获取主题信息", notes = "")
    @GetMapping({"{id:[0-9]+}/configs"})
    public Result<KafkaTopicConfigDTO> detailConfigs(@PathVariable Long id) {
        return ResultUtils.wrap(() -> {
            KafkaTopic record = kafkaTopicService.find(id);
            AssertUtils.isEmpty(record, PubError.NOT_EXISTS, "主题ID");
            return kafkaTopicManager.getConfigs(record.getClusterId(), record.getTopicName());
        });
    }

    @ApiOperation(value = "删除主题信息", notes = "")
    @DeleteMapping({"{id:[0-9]+}"})
    public Result<Integer> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            KafkaTopic record = new KafkaTopic();
            record.setId(id);
            record.setStatus(StatusS.DELETE);
            record.setUpdateBy(ContextUtil.getUsername());
            record.setUpdateAt(DateUtils.current());
            int c = kafkaTopicService.update(record);
            if (c > 0) {
                KafkaTopic topic = kafkaTopicService.find(id);
                KafkaCluster cluster = kafkaClusterService.find(topic.getClusterId());
                if (cluster != null) {
                    kafkaTopicManager.deleteTopic(cluster.getId(), Lists.newArrayList(topic.getTopicName()));
                }
            }
            return c;
        });
    }

    @ApiOperation(value = "主题刷新", notes = "注：删除原主题数据, 主题重建信息")
    @PostMapping("refresh")
    public Result<Integer> refreshTopic(@RequestBody KafkaTopic record) {
        return ResultUtils.wrapFail(() -> {
            KafkaCluster cluster = kafkaClusterService.find(record.getClusterId());
            KafkaTopic topic = kafkaTopicService.findByClusterIdAndTopicName(record.getClusterId(), record.getTopicName());
            if (cluster == null || topic == null) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS);
            }

            ZkTopicUtils.deleteTopic(cluster.getZookeeper(), topic.getTopicName());
            createTopic(cluster, topic);
            return 1;
        });
    }

    @ApiOperation(value = "同步集群主题", notes = "注：同步集群所有主题(type1.增量同步)")
    @PostMapping("sync")
    public Result<Integer> syncTopics(@RequestBody KafkaTopic record) {
        return ResultUtils.wrap(() -> {
            AssertUtils.isEmpty(record.getClusterId(), PubError.NON_NULL, "集群ID");
            if (CommonUtils.isEquals(record.getType(), "1")) {
                // 增量同步
                KafkaCluster cluster = kafkaClusterService.find(record.getClusterId());
                return kafkaClusterManager.syncTopics(cluster);

            } // 全量同步
            List<KafkaTopicDTO> topicList = kafkaClusterManager.topics(record.getClusterId(), null);
            return kafkaTopicService.saveOrUpdate(topicList, ContextUtil.getUsername());
        });
    }


    @ApiOperation(value = "清空所有主题", notes = "注：（删除并重建）")
    @PostMapping("clean")
    public Result<Integer> cleanTopics(@RequestBody KafkaTopic topic) {
        return ResultUtils.wrap(() -> {
            KafkaCluster cluster = kafkaClusterService.find(topic.getClusterId());
            if (cluster == null) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS);
            }
            KafkaTopic p1 = new KafkaTopic();
            p1.setClusterId(topic.getClusterId());
            p1.setStatus("1");
            List<KafkaTopic> topics = kafkaTopicService.find(p1);
            if (topics.isEmpty()) return 0;
            topics.parallelStream().forEach(r -> ZkTopicUtils.deleteTopic(cluster.getZookeeper(), r.getTopicName()));
            Thread.sleep(10000);
            topics.parallelStream().forEach(r -> createTopic(cluster, r));
            return topics.size();
        });
    }

}
