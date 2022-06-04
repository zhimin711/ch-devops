package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.dto.ContentSearchDTO;
import com.ch.cloud.kafka.dto.KafkaTopicDTO;
import com.ch.cloud.kafka.enums.ContentType;
import com.ch.cloud.kafka.enums.SearchType;
import com.ch.cloud.kafka.model.BtContentRecord;
import com.ch.cloud.kafka.model.ContentSearch;
import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.service.IContentRecordService;
import com.ch.cloud.kafka.service.IContentSearchService;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.cloud.kafka.tools.KafkaContentTool;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.cloud.kafka.vo.ContentQuery;
import com.ch.cloud.utils.ContextUtil;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author zhimin.ma
 * @since 2018/9/25 10:02
 */

@Api(tags = "KAFKA消息搜索模块")
@RestController
@RequestMapping("/kafka/content")
@Slf4j
public class ContentSearchController {

    @Autowired
    private KafkaClusterService   kafkaClusterService;
    @Autowired
    private KafkaTopicService     topicService;
    @Autowired
    private IContentSearchService contentSearchService;
    @Autowired
    private IContentRecordService contentRecordService;

    @ApiOperation(value = "消息搜索")
    @GetMapping("search")
    public Result<?> search(ContentQuery record) {
        Result<KafkaTopicDTO> res1 = ResultUtils.wrapFail(() -> topicService.check(record.getClusterId(), record.getTopic()));
        if (res1.isEmpty()) {
            return res1;
        }
        KafkaTopicDTO kafkaTopicDto = res1.get();
        KafkaContentTool contentTool = new KafkaContentTool(kafkaTopicDto.getZookeeper(), kafkaTopicDto.getClusterId(), kafkaTopicDto.getTopicName());
        contentTool.setContentSearchService(contentSearchService);
        contentTool.setContentRecordService(contentRecordService);
        contentTool.setUsername(ContextUtil.getUser());
        Result<BtContentRecord> res = ResultUtils.wrapList(() -> {
            SearchType searchType = SearchType.ALL;
            if ("1".equals(record.getType())) {
                searchType = SearchType.LATEST;
            } else if ("2".equals(record.getType())) {
                searchType = SearchType.EARLIEST;
            }
            if (searchType == SearchType.ALL && CommonUtils.isEmpty(record.getContent())) {
                throw ExceptionUtils.create(PubError.NOT_ALLOWED, "全量搜索，内容不能为空！");
            }
            if ((searchType == SearchType.EARLIEST || searchType == SearchType.LATEST)
                    && CommonUtils.isEmpty(record.getContent()) && record.getLimit() > 1000) {
                throw ExceptionUtils.create(PubError.NOT_ALLOWED, "无内容搜索量不能超过1000！");
            }
            ContentType contentType = ContentType.from(kafkaTopicDto.getType());
            Class<?> clazz = null;
            if (CommonUtils.isNotEmpty(kafkaTopicDto.getClassName())) {
                clazz = KafkaSerializeUtils.loadClazz(kafkaTopicDto.getClassFile(), kafkaTopicDto.getClassName());
            }
            return contentTool.searchTopicContent(contentType, searchType, record.getLimit(), record.getContent(), clazz);
        });
        Map<String, Object> extra = Maps.newHashMap();
        extra.put("contentType", kafkaTopicDto.getType());
        extra.put("searchId", contentTool.getSearchId());
        extra.put("searchAsync", contentTool.isAsync());
        res.setExtra(extra);
        return res;
    }

    @ApiOperation(value = "KAFKA消息集群")
    @GetMapping("clusters")
    public Result<KafkaCluster> getClusters() {
        return ResultUtils.wrapList(() -> kafkaClusterService.findEnabled());
    }

    @ApiOperation(value = "KAFKA消息主题")
    @GetMapping("topics")
    public Result<KafkaTopic> findTopicsByClusterName(@RequestParam("clusterId") Long clusterId,
                                                      @RequestParam("topicName") String topicName) {
        return ResultUtils.wrapList(() -> {
            KafkaCluster cluster = kafkaClusterService.find(clusterId);
            if (cluster == null) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS);
            }
            return topicService.findByClusterIdLikeTopicName(clusterId, topicName);
        });

    }

    @GetMapping("search/{sid}/status")
    public Result<String> getSearchStatus(@PathVariable Long sid) {
        return ResultUtils.wrapFail(() -> contentSearchService.find(sid).getStatus());
    }

    @GetMapping("search/{sid}/records")
    public Result<BtContentRecord> getSearchRecords(@PathVariable Long sid) {
        return ResultUtils.wrapList(() -> contentRecordService.findBySid(sid));
    }

    @PostMapping("send")
    public Result<Integer> sendMessage(@RequestBody ContentSearchDTO searchDto) {
        return ResultUtils.wrap(() -> {
            if (CommonUtils.isEmpty(searchDto.getContent())) {
                throw ExceptionUtils.create(PubError.NON_NULL, "发送消息不能为空!");
            }
            KafkaTopicDTO kafkaTopicDto = topicService.check(searchDto.getClusterId(), searchDto.getTopic());

            KafkaContentTool contentTool = new KafkaContentTool(kafkaTopicDto.getZookeeper(), kafkaTopicDto.getClusterId(), kafkaTopicDto.getTopicName());

            contentTool.send(KafkaSerializeUtils.convertContent(kafkaTopicDto, searchDto.getContent()));
        });
    }


    @PutMapping("resend/{sid}")
    public Result<Integer> resendMessage(@PathVariable Long sid, @RequestBody String content) {
        return ResultUtils.wrap(() -> {
            ContentSearch searchRecord = contentSearchService.find(sid);
            KafkaTopicDTO kafkaTopicDto = topicService.check(searchRecord.getClusterId(), searchRecord.getTopic());

            KafkaContentTool contentTool = new KafkaContentTool(kafkaTopicDto.getZookeeper(), kafkaTopicDto.getClusterId(), kafkaTopicDto.getTopicName());

            contentTool.send(KafkaSerializeUtils.convertContent(kafkaTopicDto, content));
//            BtClusterConfig config = clusterConfigService.findByClusterName(searchRecord.getCluster());
//            KafkaContentTool contentTool = new KafkaContentTool(config.getZookeeper(), searchRecord.getCluster(), searchRecord.getTopic());
//            contentTool.send(content.getBytes());
        });
    }


}
