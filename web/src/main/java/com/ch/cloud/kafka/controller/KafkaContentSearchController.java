package com.ch.cloud.kafka.controller;

import java.util.Map;

import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.tools.KafkaClusterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ch.cloud.kafka.dto.KafkaTopicDTO;
import com.ch.cloud.kafka.enums.SearchType;
import com.ch.cloud.kafka.dto.KafkaContentSearchDTO;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.cloud.kafka.tools.KafkaMessageManager;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.cloud.kafka.vo.KafkaContentSearchVO;
import com.ch.cloud.kafka.vo.KafkaMessageVO;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.AssertUtils;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Maps;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhimin.ma
 * @since 2018/9/25 10:02
 */

@Api(tags = "KAFKA消息搜索模块")
@RestController
@RequestMapping("/kafka/content")
@Slf4j
public class KafkaContentSearchController {

    @Autowired
    private KafkaClusterManager kafkaClusterManager;
    @Autowired
    private KafkaTopicService kafkaTopicService;
    @Autowired
    private KafkaMessageManager kafkaMessageManager;

    @ApiOperation(value = "消息搜索")
    @GetMapping("search")
    public Result<?> search(KafkaContentSearchVO record) {
        AssertUtils.isTrue(record.getType() == SearchType.ALL && CommonUtils.isEmpty(record.getContent()),
            PubError.NOT_ALLOWED, "全量搜索，内容不能为空！");
        KafkaTopicDTO kafkaTopicDto = kafkaTopicService.check(record.getClusterId(), record.getTopic());
        Result<KafkaContentSearchDTO> res = ResultUtils.wrap(() -> {
            TopicInfo info = kafkaClusterManager.info(record.getClusterId(), record.getTopic());
            AssertUtils.isEmpty(info.getPartitions(), PubError.NOT_EXISTS, "主题分区");
            Class<?> clazz = KafkaSerializeUtils.loadClazz(kafkaTopicDto.getClassFile(), kafkaTopicDto.getClassName());
            info.getPartitions().forEach(partition -> {
                if (CommonUtils.isNotEmpty(record.getPartition()) && record.getPartition() >= 0
                    && !CommonUtils.isEquals(partition.getPartition(), record.getPartition())) {
                    return;
                }
                kafkaMessageManager.search(record, partition, clazz);
            });
        });
        Map<String, Object> extra = Maps.newHashMap();
        extra.put("contentType", kafkaTopicDto.getType());
        res.setExtra(extra);
        return res;
    }

    @PostMapping("send")
    public Result<Integer> sendMessage(@RequestBody KafkaMessageVO messageVO) {
        return ResultUtils.wrap(() -> {
            AssertUtils.isEmpty(messageVO.getValue(), PubError.NON_NULL, "发送消息不能为空!");
            KafkaTopicDTO kafkaTopicDto = kafkaTopicService.check(messageVO.getClusterId(), messageVO.getTopic());
            kafkaMessageManager.send(messageVO,
                KafkaSerializeUtils.convertContent(kafkaTopicDto, messageVO.getValue()));
        });
    }

    @PutMapping("resend/{sid}")
    public Result<Integer> resendMessage(@PathVariable Long sid, @RequestBody String content) {
        return ResultUtils.wrap(() -> {
            // KafkaContentSearch searchRecord = contentSearchService.find(sid);
            // KafkaTopicDTO kafkaTopicDto = kafkaTopicService.check(searchRecord.getClusterId(),
            // searchRecord.getTopic());

            // BtClusterConfig config = clusterConfigService.findByClusterName(searchRecord.getCluster());
            // KafkaContentTool contentTool = new KafkaContentTool(config.getZookeeper(), searchRecord.getCluster(),
            // searchRecord.getTopic());
            // contentTool.send(content.getBytes());
        });
    }

}
