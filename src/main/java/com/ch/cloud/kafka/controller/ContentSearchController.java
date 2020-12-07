package com.ch.cloud.kafka.controller;

import com.ch.Constants;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.model.BtContentRecord;
import com.ch.cloud.kafka.model.BtContentSearch;
import com.ch.cloud.kafka.model.BtTopic;
import com.ch.cloud.kafka.pojo.*;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.IContentRecordService;
import com.ch.cloud.kafka.service.IContentSearchService;
import com.ch.cloud.kafka.service.ITopicService;
import com.ch.cloud.kafka.tools.KafkaContentTool;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.ExceptionUtils;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author zhimin.ma
 * @date 2018/9/25 10:02
 */

@Api(tags = "KAFKA消息搜索模块")
@RestController
@RequestMapping("content")
@Slf4j
public class ContentSearchController {

    @Autowired
    private ClusterConfigService clusterConfigService;
    @Autowired
    private ITopicService topicService;
    @Autowired
    private IContentSearchService contentSearchService;
    @Autowired
    private IContentRecordService contentRecordService;

    @ApiOperation(value = "消息搜索")
    @GetMapping("search")
    public Result<?> search(ContentQuery record,
                            @RequestHeader(Constants.TOKEN_USER) String username) {
        Result<TopicDto> res1 = ResultUtils.wrapFail(() -> topicService.check(record.getCluster(), record.getTopic()));
        if (res1.isEmpty()) {
            return res1;
        }
        TopicDto topicDto = res1.get();
        KafkaContentTool contentTool = new KafkaContentTool(topicDto.getZookeeper(), topicDto.getClusterName(), topicDto.getTopicName());
        contentTool.setContentSearchService(contentSearchService);
        contentTool.setContentRecordService(contentRecordService);
        contentTool.setUsername(username);
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
            ContentType contentType = ContentType.from(topicDto.getType());
            Class<?> clazz = null;
            if (CommonUtils.isNotEmpty(topicDto.getClassName())) {
                clazz = KafkaSerializeUtils.loadClazz(topicDto.getClassFile(), topicDto.getClassName());
            }
            return contentTool.searchTopicContent(contentType, searchType, record.getLimit(), record.getContent(), clazz);
        });
        Map<String, Object> extra = Maps.newHashMap();
        extra.put("contentType", topicDto.getType());
        extra.put("searchId", contentTool.getSearchId());
        extra.put("searchAsync", contentTool.isAsync());
        res.setExtra(extra);
        return res;
    }

    @GetMapping("clusters")
    public Result<BtClusterConfig> getClusters() {
        return ResultUtils.wrapList(() -> clusterConfigService.findEnabled());
    }

    @GetMapping("topics")
    public Result<BtTopic> findTopicsByClusterName(@RequestParam("clusterName") String clusterName,
                                                   @RequestParam("topicName") String topicName) {
        return ResultUtils.wrapList(() -> {
            BtClusterConfig cluster = clusterConfigService.findByClusterName(clusterName);
            if (cluster == null) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS);
            }
            return topicService.findByClusterLikeTopic(cluster.getClusterName(), topicName);
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
    public Result<Integer> sendMessage(@RequestBody ContentSearchDto searchDto) {
        return ResultUtils.wrapFail(() -> {
            if (CommonUtils.isEmpty(searchDto.getContent())) {
                throw ExceptionUtils.create(PubError.NON_NULL, "发送消息不能为空!");
            }
            TopicDto topicDto = topicService.check(searchDto.getCluster(), searchDto.getTopic());

            KafkaContentTool contentTool = new KafkaContentTool(topicDto.getZookeeper(), topicDto.getClusterName(), topicDto.getTopicName());

            contentTool.send(KafkaSerializeUtils.convertContent(topicDto, searchDto.getContent()));
            return 1;
        });
    }


    @PutMapping("resend/{sid}")
    public Result<Integer> resendMessage(@PathVariable Long sid, @RequestBody String content) {
        return ResultUtils.wrapFail(() -> {
            BtContentSearch searchRecord = contentSearchService.find(sid);
            TopicDto topicDto = topicService.check(searchRecord.getCluster(), searchRecord.getTopic());

            KafkaContentTool contentTool = new KafkaContentTool(topicDto.getZookeeper(), topicDto.getClusterName(), topicDto.getTopicName());

            contentTool.send(KafkaSerializeUtils.convertContent(topicDto, content));
            return 1;
//            BtClusterConfig config = clusterConfigService.findByClusterName(searchRecord.getCluster());
//            KafkaContentTool contentTool = new KafkaContentTool(config.getZookeeper(), searchRecord.getCluster(), searchRecord.getTopic());
//            contentTool.send(content.getBytes());
//            return 1;
        });
    }


}
