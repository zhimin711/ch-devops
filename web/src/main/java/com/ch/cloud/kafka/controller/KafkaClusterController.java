package com.ch.cloud.kafka.controller;

import com.ch.StatusS;
import com.ch.cloud.kafka.dto.BrokerDTO;
import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.cloud.kafka.tools.KafkaClusterManager;
import com.ch.cloud.kafka.tools.KafkaClusterUtils;
import com.ch.cloud.utils.ContextUtil;
import com.ch.e.PubError;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.DateUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author zhimin.ma
 * @since 2018/9/25 20:29
 */
@Api(tags = "KAFKA集群配置模块")
@RestController
@RequestMapping("/kafka/cluster")
public class KafkaClusterController {

    @Autowired
    private KafkaClusterService kafkaClusterService;
    @Autowired
    private KafkaTopicService kafkaTopicService;

    @Autowired
    private KafkaClusterManager kafkaClusterManager;

    @GetMapping(value = {"{num:\\d+}/{size:\\d+}"})
    public PageResult<KafkaCluster> page(KafkaCluster record,
                                         @PathVariable(value = "num") int pageNum,
                                         @PathVariable(value = "size") int pageSize) {
        PageInfo<KafkaCluster> pageInfo = kafkaClusterService.findPage(record, pageNum, pageSize);
        return PageResult.success(pageInfo.getTotal(), pageInfo.getList());
    }

    @ApiOperation(value = "新增Kafka集群", notes = "")
    @PostMapping
    public Result<Integer> add(@RequestBody KafkaCluster record) {
        KafkaCluster r = kafkaClusterService.findByClusterName(record.getClusterName());
        if (r != null) {
            return Result.error(PubError.EXISTS);
        }
        record.setStatus(StatusS.BEGIN);
        record.setCreateBy(ContextUtil.getUser());
        return ResultUtils.wrapFail(() -> kafkaClusterService.save(record));
    }

    @PutMapping({"{id:\\d+}"})
    public Result<Integer> edit(@PathVariable Long id, @RequestBody KafkaCluster record) {
        return ResultUtils.wrapFail(() -> {
            record.setUpdateBy(ContextUtil.getUser());
            record.setUpdateAt(DateUtils.current());
            return kafkaClusterService.update(record);
        });
    }

    @DeleteMapping({"{id:\\d+}"})
    public Result<Integer> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            KafkaCluster c = kafkaClusterService.find(id);

            KafkaTopic srcRecord = new KafkaTopic();
            srcRecord.setClusterId(id);
            srcRecord.setStatus(StatusS.ENABLED);
            KafkaTopic targetRecord = new KafkaTopic();
            targetRecord.setStatus(StatusS.DELETE);
            targetRecord.setUpdateBy(ContextUtil.getUser());
            targetRecord.setUpdateAt(DateUtils.current());
            int c2 = kafkaTopicService.update(srcRecord, targetRecord);
            return kafkaClusterService.delete(id);
        });
    }


    @GetMapping("{id:\\d+}")
    public Result<KafkaCluster> detail(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            KafkaCluster config = kafkaClusterService.find(id);
            Set<String> topicNames = KafkaClusterUtils.fetchTopicNames(config);
            config.setTopicCount(topicNames.size());
            config.setBrokerCount(KafkaClusterUtils.countBroker(config));
            config.setConsumerCount(KafkaClusterUtils.countConsumerGroup(config));
            return config;
        });
    }

    @GetMapping("{id:\\d+}/brokers")
    public Result<BrokerDTO> brokers(@PathVariable Long id) {
        return ResultUtils.wrapList(() -> {
            KafkaCluster config = kafkaClusterService.find(id);
            return kafkaClusterManager.brokers("", config);
        });
    }

}
