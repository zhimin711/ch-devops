package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.dto.KafkaContentSearchDTO;
import com.ch.cloud.kafka.dto.KafkaMessageDTO;
import com.ch.cloud.kafka.dto.KafkaTopicDTO;
import com.ch.cloud.kafka.enums.SearchType;
import com.ch.cloud.kafka.model.KafkaTopic;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author zhimin.ma
 * @since 2018/9/25 10:02
 */

@Tag(name = "KAFKA消息搜索服务")
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
    
    @Operation(summary = "消息搜索")
    @GetMapping("search")
    public Result<?> search(KafkaContentSearchVO searchVO) {
        AssertUtils.isTrue(searchVO.getType() == SearchType.ALL && CommonUtils.isEmpty(searchVO.getContent()),
                PubError.NOT_ALLOWED, "全量搜索，内容不能为空！");
        KafkaTopic kafkaTopic = kafkaTopicService.check(searchVO.getClusterId(), searchVO.getTopicId());
        Result<KafkaContentSearchDTO> result = ResultUtils.wrap(() -> {
            searchVO.setTopic(kafkaTopic.getTopicName());
            TopicInfo info = kafkaClusterManager.info(searchVO.getClusterId(), kafkaTopic.getTopicName());
            AssertUtils.isEmpty(info.getPartitions(), PubError.NOT_EXISTS, "主题分区");
            Class<?> clazz = KafkaSerializeUtils.loadClazz(kafkaTopic.getClassFile(), kafkaTopic.getClassName());
            KafkaContentSearchDTO searchDTO = new KafkaContentSearchDTO();
            searchDTO.setClusterId(searchVO.getClusterId());
            searchDTO.setTopic(kafkaTopic.getTopicName());
            info.getPartitions().forEach(partition -> {
                if (CommonUtils.isNotEmpty(searchVO.getPartition()) && searchVO.getPartition() >= 0
                        && !CommonUtils.isEquals(partition.getPartition(), searchVO.getPartition())) {
                    return;
                }
                List<KafkaMessageDTO> list = kafkaMessageManager.search(searchVO, partition, clazz);
                
                searchDTO.putOffset(partition.getPartition(), searchVO.getOffset());
                searchDTO.putMessages(partition.getPartition(), Lists.reverse(list));
            });
            return searchDTO;
        });
        Map<String, Object> extra = Maps.newHashMap();
        extra.put("contentType", kafkaTopic.getType());
        result.setExtra(extra);
        return result;
    }
    
    @Operation(summary = "更多消息")
    @GetMapping("more")
    public Result<?> more(KafkaContentSearchVO searchVO) {
        AssertUtils.isTrue(CommonUtils.isEmptyOr(searchVO.getPartition(), searchVO.getOffset()), PubError.NOT_ALLOWED,
                "条件不足！");
        KafkaTopic kafkaTopic = kafkaTopicService.check(searchVO.getClusterId(), searchVO.getTopicId());
        Result<KafkaMessageDTO> result = ResultUtils.wrap(() -> {
            searchVO.setTopic(kafkaTopic.getTopicName());
            Class<?> clazz = KafkaSerializeUtils.loadClazz(kafkaTopic.getClassFile(), kafkaTopic.getClassName());
            return kafkaMessageManager.more(searchVO, clazz);
        });
        Map<String, Object> extra = Maps.newHashMap();
        extra.put("offset", searchVO.getOffset());
        result.setExtra(extra);
        return result;
    }
    
    @Operation(summary = "发送消息")
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
    
    @Operation(summary = "分区计算")
    @Parameters({@Parameter(name = "partitionKey", description = "分区Key"),
    @Parameter(name = "partitionKey", description = "分区Key")})
    @GetMapping("calc-partition")
    public Result<Integer> calcPartition(@RequestParam String partitionKey, @RequestParam int partitionCount) {
        return ResultUtils.wrap(() -> {
            AssertUtils.isEmpty(partitionKey, PubError.NON_NULL, "分区Key");
            AssertUtils.isFalse(partitionCount > 0, PubError.INVALID, "分区数量<1");
            return Utils.toPositive(Utils.murmur2(partitionKey.getBytes())) % partitionCount;
        });
    }
    
}
