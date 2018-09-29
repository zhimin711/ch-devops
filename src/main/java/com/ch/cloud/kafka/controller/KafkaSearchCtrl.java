package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.tools.TopicManager;
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
}
