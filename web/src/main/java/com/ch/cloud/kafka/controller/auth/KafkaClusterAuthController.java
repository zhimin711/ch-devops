package com.ch.cloud.kafka.controller.auth;

import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.cloud.kafka.tools.KafkaClusterManager;
import com.ch.e.PubError;
import com.ch.pojo.VueRecord;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.AssertUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.VueRecordUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhimin.ma
 * @since 2018/9/25 20:29
 */
@Tag(name = "KAFKA集群配置模块")
@RestController
@RequestMapping("/kafka/cluster")
public class KafkaClusterAuthController {

    @Autowired
    private KafkaClusterService kafkaClusterService;
    @Autowired
    private KafkaTopicService kafkaTopicService;
    @Autowired
    private KafkaClusterManager kafkaClusterManager;

    @GetMapping("available")
    public Result<KafkaCluster> getAvailable() {
        return ResultUtils.wrapList(() -> kafkaClusterService.findEnabled());
    }


    @GetMapping("{id:\\d+}/topics")
    public Result<VueRecord> getTopics(@PathVariable("id") Long id,
                                       @RequestParam("topicName") String topicName) {
        return ResultUtils.wrapList(() -> {
            List<KafkaTopic> list = kafkaTopicService.findByClusterIdLikeTopicName(id, topicName);
            return VueRecordUtils.covertTree(list, "id", "topicName", null);
        });
    }

    @Operation(summary = "获取主题信息")
    @GetMapping({"{id:\\d+}/topic/{topicId:\\d+}"})
    public Result<TopicInfo> topicInfo(@PathVariable Long id, @PathVariable Long topicId) {
        return ResultUtils.wrapFail(() -> {
            KafkaTopic record = kafkaTopicService.find(topicId);
            AssertUtils.isEmpty(record, PubError.NOT_EXISTS, "主题ID");
            AssertUtils.isFalse(CommonUtils.isEquals(id, record.getClusterId()), PubError.NOT_ALLOWED, "集群与主题 id not same");
            return kafkaClusterManager.info(id, record.getTopicName());
        });
    }
}
