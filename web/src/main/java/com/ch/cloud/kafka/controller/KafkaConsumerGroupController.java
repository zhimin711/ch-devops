package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.dto.ConsumerGroupDTO;
import com.ch.cloud.kafka.dto.ConsumerGroupDescribeDTO;
import com.ch.cloud.kafka.tools.KafkaConsumerGroupManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author zhimin.ma
 * @date 2022/3/27 7:11 下午
 */
@RequestMapping("/kafka/consumerGroups")
@RestController
public class KafkaConsumerGroupController {

    @Autowired
    private KafkaConsumerGroupManager kafkaConsumerGroupManager;

    @GetMapping("{clusterId}")
    public List<ConsumerGroupDTO> list(@PathVariable Long clusterId, String groupId) throws ExecutionException, InterruptedException {
        return kafkaConsumerGroupManager.consumerGroup(clusterId, groupId);
    }

    @GetMapping("/{clusterId}/{groupId}")
    public ConsumerGroupDTO info(@PathVariable String groupId, @PathVariable Long clusterId) throws ExecutionException, InterruptedException {
        return kafkaConsumerGroupManager.info(clusterId, groupId);
    }

    @GetMapping("/{clusterId}/{groupId}/describe")
    public List<ConsumerGroupDescribeDTO> describe(@PathVariable String groupId, @PathVariable Long clusterId) throws ExecutionException, InterruptedException {
        return kafkaConsumerGroupManager.describe(clusterId, groupId);
    }

    @DeleteMapping("/{clusterId}/{groupId}")
    public void delete(@PathVariable String groupId, @PathVariable Long clusterId) throws ExecutionException, InterruptedException {
        kafkaConsumerGroupManager.delete(clusterId, groupId);
    }
}
