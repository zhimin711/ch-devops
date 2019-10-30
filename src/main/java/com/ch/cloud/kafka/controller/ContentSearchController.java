package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.model.BtContentRecord;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.pojo.ContentQuery;
import com.ch.cloud.kafka.pojo.ContentType;
import com.ch.cloud.kafka.pojo.SearchType;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.IContentRecordService;
import com.ch.cloud.kafka.service.IContentSearchService;
import com.ch.cloud.kafka.service.TopicExtService;
import com.ch.cloud.kafka.tools.KafkaContentTool;
import com.ch.cloud.kafka.tools.TopicManager;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.ExceptionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;

/**
 * @author 01370603
 * @date 2018/9/25 10:02
 */

@Api(tags = "KAFKA消息搜索模块")
@RestController
@RequestMapping("content")
public class ContentSearchController {

    @Value("${share.path.libs}")
    private String libsDir;

    @Autowired
    private ClusterConfigService clusterConfigService;
    @Autowired
    private TopicExtService topicExtService;
    @Autowired
    private IContentSearchService contentSearchService;
    @Autowired
    private IContentRecordService contentRecordService;

    @ApiOperation(value = "消息搜索")
    @GetMapping("search")
    public Result<BtContentRecord> search(ContentQuery record) {
        return ResultUtils.wrapList(() -> {
            BtClusterConfig config = clusterConfigService.findByClusterName(record.getCluster());
            if (config == null) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS, record.getCluster() + "集群配置不存在!");
            }
            BtTopicExt topicExt = topicExtService.findByClusterAndTopic(record.getCluster(), record.getTopic());
            if (topicExt == null) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS, record.getCluster() + ":" + record.getTopic() + "主题配置不存在！");
            }
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
            KafkaContentTool contentTool = new KafkaContentTool(config.getZookeeper(), topicExt.getClusterName(), topicExt.getTopicName());
            contentTool.setContentSearchService(contentSearchService);
            contentTool.setContentRecordService(contentRecordService);
            ContentType contentType = ContentType.from(topicExt.getType());
            Class<?> clazz = null;
            if (CommonUtils.isNotEmpty(topicExt.getClassName())) {
                clazz = KafkaSerializeUtils.loadClazz(libsDir + File.separator + topicExt.getClassFile(), topicExt.getClassName());
            }
            return contentTool.searchTopicContent(contentType, searchType, record.getLimit(), record.getContent(), clazz);
        });
    }

    @GetMapping("clusters")
    public Result<BtClusterConfig> getClusters() {
        return ResultUtils.wrapList(() -> clusterConfigService.findEnabled());
    }

    @GetMapping("topics")
    public Result<BtTopicExt> findTopicsByClusterName(@RequestParam("clusterName") String clusterName,
                                                      @RequestParam("topicName") String topicName) {
        return ResultUtils.wrapList(() -> {
            BtClusterConfig cluster = clusterConfigService.findByClusterName(clusterName);
            if (cluster == null) {
                throw ExceptionUtils.create(PubError.NOT_EXISTS);
            }
            return topicExtService.findByClusterLikeTopic(cluster.getClusterName(), topicName);
        });

    }

    @GetMapping("search/{sid}")
    public Result<String> getSearchStatus(@PathVariable Long sid) {
        return ResultUtils.wrapFail(() -> "0");
    }

}
