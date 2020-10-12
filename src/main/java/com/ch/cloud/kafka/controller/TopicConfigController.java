package com.ch.cloud.kafka.controller;

import com.ch.Constants;
import com.ch.StatusS;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.model.BtTopic;
import com.ch.cloud.kafka.pojo.TopicConfig;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.ITopicService;
import com.ch.cloud.kafka.tools.TopicManager;
import com.ch.e.PubError;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.ch.utils.ExceptionUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zhimin.ma
 * @date 2018/9/25 20:29
 */
@Api(tags = "KAFKA主题配置模块")
@RestController
@RequestMapping("topic")
public class TopicConfigController {

    @Autowired
    private ITopicService topicService;
    @Autowired
    private ClusterConfigService clusterConfigService;

    @ApiOperation(value = "分页查询", notes = "需要在请求头中附带token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "num", value = "页码", required = true),
            @ApiImplicitParam(name = "size", value = "分页大小", required = true),
            @ApiImplicitParam(name = "record", value = "查询条件", paramType = "query")
    })
    @GetMapping(value = {"{num}/{size}"})
    public PageResult<BtTopic> page(BtTopic record,
                                    @PathVariable(value = "num") int pageNum,
                                    @PathVariable(value = "size") int pageSize) {
        PageInfo<BtTopic> pageInfo = topicService.findPage(record, pageNum, pageSize);
        return PageResult.success(pageInfo.getTotal(), pageInfo.getList());

    }

    @ApiOperation(value = "新增主题信息", notes = "")
    @PostMapping
    public Result<Integer> add(@RequestBody BtTopic record,
                               @RequestHeader(Constants.TOKEN_USER) String username) {
        BtTopic r = topicService.findByClusterAndTopic(record.getClusterName(), record.getTopicName());
        if (r != null && !CommonUtils.isEquals(r.getStatus(), StatusS.DELETE)) {
            return Result.error(PubError.EXISTS, "主题已存在！");
        }
        return ResultUtils.wrapFail(() -> {
            BtClusterConfig cluster = clusterConfigService.findByClusterName(record.getClusterName());
            TopicInfo info = TopicManager.getInfo(cluster.getZookeeper(), record.getTopicName());
            if (info == null) {
                createTopic(cluster, record);
            } else {
                record.setPartitionSize(info.getPartitionSize());
                record.setReplicaSize(info.getReplicaSize());
            }

            if (r != null) { // 已删除主题重建
                r.setPartitionSize(record.getPartitionSize());
                r.setReplicaSize(record.getReplicaSize());
                r.setType(record.getType());
                r.setClassFile(record.getClassFile());
                r.setClusterName(record.getClassName());
                r.setDescription(record.getDescription());
                r.setStatus(StatusS.ENABLED);
                r.setUpdateBy(username);
                r.setUpdateAt(DateUtils.current());
                return topicService.update(r);
            }
            record.setCreateBy(username);
            record.setStatus(StatusS.ENABLED);
            return topicService.save(record);
        });
    }

    private void createTopic(BtClusterConfig cluster, BtTopic record) {
        TopicConfig config = new TopicConfig();
        config.setZookeeper(cluster.getZookeeper());
        config.setTopicName(record.getTopicName());
        config.setPartitions(record.getPartitionSize());
        config.setReplicationFactor(record.getReplicaSize());
//        TopicManager.createTopic(config);
        TopicManager.createTopicByCommand(config);
    }

    @ApiOperation(value = "修改主题信息", notes = "")
    @PutMapping({"{id}"})
    public Result<Integer> update(@PathVariable Long id, @RequestBody BtTopic record,
                                  @RequestHeader(Constants.TOKEN_USER) String username) {
        return ResultUtils.wrapFail(() -> {
            record.setUpdateBy(username);
            record.setUpdateAt(DateUtils.current());
            return topicService.update(record);
        });
    }

    @ApiOperation(value = "删除主题信息", notes = "")
    @DeleteMapping({"{id}"})
    public Result<Integer> delete(@PathVariable Long id,
                                  @RequestHeader(Constants.TOKEN_USER) String username) {
        return ResultUtils.wrapFail(() -> {
            BtTopic record = new BtTopic();
            record.setId(id);
            record.setStatus(StatusS.DELETE);
            record.setUpdateBy(username);
            record.setUpdateAt(DateUtils.current());
            int c = topicService.update(record);
            if (c > 0) {
                BtTopic topic = topicService.find(id);
                BtClusterConfig cluster = clusterConfigService.findByClusterName(topic.getClusterName());
                TopicManager.deleteTopic(cluster.getZookeeper(), topic.getTopicName());
            }
            return c;
        });
    }

    @GetMapping("clusters")
    public Result<BtClusterConfig> getClusters() {
        return ResultUtils.wrapList(() -> clusterConfigService.findEnabled());
    }

    @GetMapping("topics")
    public Result<String> getTopicsByClusterName(@RequestParam("clusterName") String clusterName,
                                                 @RequestParam("topicName") String topicName) {
        return ResultUtils.wrapList(() -> {
            BtClusterConfig cluster = clusterConfigService.findByClusterName(clusterName);
            if (cluster == null) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS);
            }
            return TopicManager.getTopicsByName(cluster.getZookeeper(), topicName);
        });
    }


    @ApiOperation(value = "主题刷新", notes = "注：删除原主题数据, 主题重建信息")
    @PostMapping("refresh")
    public Result<Integer> refreshTopic(@RequestBody BtTopic record) {
        return ResultUtils.wrapFail(() -> {
            BtClusterConfig cluster = clusterConfigService.findByClusterName(record.getClusterName());
            BtTopic topic = topicService.findByClusterAndTopic(record.getClusterName(), record.getTopicName());
            if (cluster == null || topic == null) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS);
            }

            TopicManager.deleteTopic(cluster.getZookeeper(), topic.getTopicName());
            createTopic(cluster, topic);
            return 1;
        });
    }

    @ApiOperation(value = "同步集群主题", notes = "注：同步集群所有主题")
    @PostMapping("sync")
    public Result<Integer> syncTopics(@RequestBody BtTopic topic,
                                      @RequestHeader(Constants.TOKEN_USER) String username) {
        return ResultUtils.wrap(() -> {
            BtClusterConfig cluster = clusterConfigService.findByClusterName(topic.getClusterName());
            if (cluster == null) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS);
            }
            List<TopicInfo> topicList = TopicManager.getTopics(cluster.getZookeeper());
            return topicService.saveOrUpdate(topicList, topic.getClusterName(), username);
        });
    }


    @ApiOperation(value = "清空所有主题", notes = "注：（删除并重建）")
    @PostMapping("clean")
    public Result<Integer> cleanTopics(@RequestBody BtTopic topic,
                                       @RequestHeader(Constants.TOKEN_USER) String username) {
        return ResultUtils.wrap(() -> {
            BtClusterConfig cluster = clusterConfigService.findByClusterName(topic.getClusterName());
            if (cluster == null) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS);
            }
            BtTopic p1 = new BtTopic();
            p1.setClusterName(topic.getClusterName());
            p1.setStatus("1");
            List<BtTopic> topics = topicService.find(p1);
            if (topics.isEmpty()) return 0;
            topics.parallelStream().forEach(r -> TopicManager.deleteTopic(cluster.getZookeeper(), r.getTopicName()));
            Thread.sleep(10000);
            topics.parallelStream().forEach(r -> createTopic(cluster, r));
            return topics.size();
        });
    }

}
