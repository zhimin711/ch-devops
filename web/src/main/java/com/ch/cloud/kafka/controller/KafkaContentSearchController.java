package com.ch.cloud.kafka.controller;

import java.util.List;
import java.util.Map;

import com.ch.cloud.kafka.model.KafkaTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.ch.cloud.kafka.dto.KafkaContentSearchDTO;
import com.ch.cloud.kafka.dto.KafkaMessageDTO;
import com.ch.cloud.kafka.dto.KafkaTopicDTO;
import com.ch.cloud.kafka.enums.SearchType;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.cloud.kafka.tools.KafkaClusterManager;
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

@Api(tags = "KAFKA消息搜索服务")
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
        KafkaTopic kafkaTopic = kafkaTopicService.check(record.getClusterId(), record.getTopicId());
        Result<KafkaContentSearchDTO> res = ResultUtils.wrap(() -> {
            TopicInfo info = kafkaClusterManager.info(record.getClusterId(), kafkaTopic.getTopicName());
            AssertUtils.isEmpty(info.getPartitions(), PubError.NOT_EXISTS, "主题分区");
            Class<?> clazz = KafkaSerializeUtils.loadClazz(kafkaTopic.getClassFile(), kafkaTopic.getClassName());
            KafkaContentSearchDTO searchDTO = new KafkaContentSearchDTO();
            searchDTO.setClusterId(record.getClusterId());
            searchDTO.setTopic(kafkaTopic.getTopicName());
            info.getPartitions().forEach(partition -> {
                if (CommonUtils.isNotEmpty(record.getPartition()) && record.getPartition() >= 0
                    && !CommonUtils.isEquals(partition.getPartition(), record.getPartition())) {
                    return;
                }
                List<KafkaMessageDTO> list = kafkaMessageManager.search(record, partition, clazz);
                searchDTO.putOffset(partition.getPartition(), record.getOffset());
                searchDTO.putMessages(partition.getPartition(), list);
            });
            return searchDTO;
        });
        Map<String, Object> extra = Maps.newHashMap();
        extra.put("contentType", kafkaTopic.getType());
        res.setExtra(extra);
        return res;
    }

    @PostMapping("send")
    public Result<Integer> sendMessage(@RequestBody KafkaMessageVO messageVO) {
        return ResultUtils.wrap(() -> {
            AssertUtils.isEmpty(messageVO.getValue(), PubError.NON_NULL, "发送消息不能为空!");
            KafkaTopicDTO kafkaTopicDto = kafkaTopicService.check(messageVO.getClusterId(), messageVO.getTopic());
            if (CommonUtils.isEmpty(kafkaTopicDto.getClassName())) {
                kafkaMessageManager.send(messageVO);
            } else {
                kafkaMessageManager.send(messageVO,
                    KafkaSerializeUtils.convertContent(kafkaTopicDto, messageVO.getValue()));
            }
        });
    }

}
