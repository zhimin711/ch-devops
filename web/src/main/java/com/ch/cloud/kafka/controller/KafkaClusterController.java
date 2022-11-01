package com.ch.cloud.kafka.controller;

import java.util.Set;

import com.ch.cloud.kafka.vo.KafkaClusterVO;
import com.ch.utils.BeanUtilsV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ch.StatusS;
import com.ch.cloud.kafka.dto.BrokerDTO;
import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.cloud.kafka.tools.KafkaClusterManager;
import com.ch.cloud.kafka.tools.KafkaClusterUtils;
import com.ch.e.PubError;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.DateUtils;
import com.github.pagehelper.PageInfo;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;

/**
 * @author zhimin.ma
 * @since 2018/9/25 20:29
 */
@Api(tags = "KAFKA集群配置")
@RestController
@RequestMapping("/kafka/cluster")
public class KafkaClusterController {

    @Autowired
    private KafkaClusterService kafkaClusterService;
    @Autowired
    private KafkaTopicService kafkaTopicService;

    @Autowired
    private KafkaClusterManager kafkaClusterManager;

    @Operation(summary = "分页查询集群", description = "分页查询集群")
    @GetMapping(value = {"{num:\\d+}/{size:\\d+}"})
    public PageResult<KafkaCluster> page(KafkaClusterVO record, @PathVariable(value = "num") int pageNum,
        @PathVariable(value = "size") int pageSize) {
        PageInfo<KafkaCluster> pageInfo =
            kafkaClusterService.findPage(BeanUtilsV2.clone(record, KafkaCluster.class), pageNum, pageSize);
        return PageResult.success(pageInfo.getTotal(), pageInfo.getList());
    }

    @Operation(summary = "新增集群", description = "新增集群")
    @PostMapping
    public Result<Integer> add(@RequestBody KafkaCluster record) {
        KafkaCluster r = kafkaClusterService.findByClusterName(record.getClusterName());
        if (r != null) {
            return Result.error(PubError.EXISTS);
        }
        record.setStatus(StatusS.ENABLED);
        record.setCreateBy(ContextUtil.getUsername());
        return ResultUtils.wrapFail(() -> kafkaClusterService.save(record));
    }

    @Operation(summary = "修改集群", description = "修改集群信息")
    @PutMapping({"{id:\\d+}"})
    public Result<Integer> edit(@PathVariable Long id, @RequestBody KafkaCluster record) {
        return ResultUtils.wrapFail(() -> {
            record.setUpdateBy(ContextUtil.getUsername());
            record.setUpdateAt(DateUtils.current());
            return kafkaClusterService.update(record);
        });
    }

    @Operation(summary = "删除集群", description = "根据ID删除集群")
    @DeleteMapping({"{id:\\d+}"})
    public Result<Integer> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            KafkaCluster c = kafkaClusterService.find(id);

            KafkaTopic srcRecord = new KafkaTopic();
            srcRecord.setClusterId(id);
            srcRecord.setStatus(StatusS.ENABLED);
            KafkaTopic targetRecord = new KafkaTopic();
            targetRecord.setStatus(StatusS.DELETE);
            targetRecord.setUpdateBy(ContextUtil.getUsername());
            targetRecord.setUpdateAt(DateUtils.current());
            int c2 = kafkaTopicService.update(srcRecord, targetRecord);
            return kafkaClusterService.delete(id);
        });
    }

    @Operation(summary = "查询集群详情", description = "查询集群详情")
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

    @Operation(summary = "查询集群Brokers", description = "查询集群Brokers")
    @GetMapping("{id:\\d+}/brokers")
    public Result<BrokerDTO> brokers(@PathVariable Long id) {
        return ResultUtils.wrapList(() -> {
            KafkaCluster config = kafkaClusterService.find(id);
            return kafkaClusterManager.brokers("", config);
        });
    }

}
