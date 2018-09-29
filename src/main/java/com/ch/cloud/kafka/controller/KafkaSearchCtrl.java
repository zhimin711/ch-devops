package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.tools.KafkaTool;
import com.ch.cloud.kafka.tools.TopicManager;
import com.ch.err.ErrorCode;
import com.ch.result.HttpResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 描述：com.ch.cloud.kafka.controller
 *
 * @author zhimin
 * 2018/2/22.
 * @version 1.0
 * @since 1.8
 */
@RestController
@RequestMapping("kafka")
@Api(value = "KafkaController", description = "Kafka搜索接口")
public class KafkaSearchCtrl {


    @ApiOperation(value = "主题搜索", notes = "主题搜索接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zkUrl", value = "zk地址", required = true, dataType = "string")
            , @ApiImplicitParam(name = "topic", value = "主题", required = true, dataType = "string")
    })
    @PostMapping("/topic/search")
    public HttpResult<String> topicSearch(String zkUrl, String topic) {
        List<String> topics = TopicManager.findTopic(zkUrl, topic);
        return new HttpResult<>(topics);
    }

    @ApiOperation(value = "主题校验(是否存在)", notes = "主题校验接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zkUrl", value = "zk地址", required = true, dataType = "string")
            , @ApiImplicitParam(name = "topic", value = "主题", required = true, dataType = "string")
    })
    @PostMapping("/topic/exists")
    public HttpResult<Boolean> topicExists(String zkUrl, String topic) {
        boolean isExists = TopicManager.exists(zkUrl, topic);
        return new HttpResult<>(isExists);
    }

    @ApiOperation(value = "主题内容搜索", notes = "主题内容搜索接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zkUrl", value = "zk地址", required = true, dataType = "string")
            , @ApiImplicitParam(name = "topic", value = "主题", required = true, dataType = "string")
            , @ApiImplicitParam(name = "searchType", value = "搜索类型", required = true, dataTypeClass = KafkaTool.SearchType.class)
            , @ApiImplicitParam(name = "searchContent", value = "搜索内容", required = true, dataType = "string")
    })
    @PostMapping("/topic/content/search")
    public HttpResult<String> topicContentSearch(String zkUrl, String topic
            , KafkaTool.SearchType searchType, String searchContent) {
        KafkaTool kafkaTool = new KafkaTool(zkUrl);
        List<String> contentList = kafkaTool.searchTopicStringContent(topic, searchContent, searchType);
        return new HttpResult<>(contentList);
    }

    @ApiOperation(value = "主题内容搜索2", notes = "主题内容搜索接口2")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "zkUrl", value = "zk地址", required = true, dataType = "string")
            , @ApiImplicitParam(name = "topic", value = "主题", required = true, dataType = "string")
            , @ApiImplicitParam(name = "searchType", value = "搜索类型", required = true, dataTypeClass = KafkaTool.SearchType.class)
            , @ApiImplicitParam(name = "searchContent", value = "搜索内容", required = true, dataType = "string")
            , @ApiImplicitParam(name = "searchClass", value = "序列化类名", required = true, dataType = "string")
    })
    @PostMapping("/topic/content/search2")
    public HttpResult<String> topicContentSearch2(String zkUrl, String topic
            , KafkaTool.SearchType searchType, String searchContent, String searchClass) {
        KafkaTool kafkaTool = new KafkaTool(zkUrl);
        try {
            Class<?> clazz = Class.forName(searchClass);
            List<String> contentList = kafkaTool.searchTopicProtostuffContent(topic, searchContent, clazz, searchType);
            return new HttpResult<>(contentList);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return new HttpResult<>(ErrorCode.NOT_EXISTS, searchClass + "不存在！");
        }
    }
}
