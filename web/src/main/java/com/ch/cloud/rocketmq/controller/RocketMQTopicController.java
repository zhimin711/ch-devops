/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ch.cloud.rocketmq.controller;

import javax.annotation.Resource;

import com.ch.cloud.rocketmq.manager.RMQConsumerManager;
import com.ch.cloud.rocketmq.manager.RMQTopicManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ch.cloud.rocketmq.model.request.SendTopicMessageRequest;
import com.ch.cloud.rocketmq.model.request.TopicConfigInfo;
import com.ch.cloud.rocketmq.util.JsonUtil;
import com.ch.utils.CommonUtils;
import com.google.common.base.Preconditions;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Rocket MQ 主题管理模块")
@RestController
@RequestMapping("/rocketmq/topic")
@Slf4j
public class RocketMQTopicController {
    
    @Resource
    private RMQTopicManager rmqTopicManager;
    
    @Resource
    private RMQConsumerManager rmqConsumerManager;
    
    @Operation(summary = "查询所有主题", description = "获取当前 NameServer 下的所有主题列表", tags = {"Topic"})
    @GetMapping
    public Object list() {
        return rmqTopicManager.fetchAllTopicList();
    }
    
    @Operation(summary = "获取主题统计信息", description = "获取指定主题的消息统计信息", tags = {
            "Topic"}, parameters = {
            @Parameter(name = "topic", description = "主题名称", required = true, example = "TestTopic")})
    @GetMapping(value = "/stats")
    public Object stats(@RequestParam String topic) {
        return rmqTopicManager.stats(topic);
    }
    
    @Operation(summary = "获取主题路由信息", description = "获取指定主题的路由信息（Broker、Queue等）", tags = {
            "Topic"}, parameters = {
            @Parameter(name = "topic", description = "主题名称", required = true, example = "TestTopic")})
    @GetMapping(value = "/route")
    public Object route(@RequestParam String topic) {
        return rmqTopicManager.route(topic);
    }
    
    @Operation(summary = "查询消费者的消费统计", description = "根据主题查询消费者的消费统计详情", tags = {
            "Topic"}, parameters = {
            @Parameter(name = "topic", description = "主题名称", required = true, example = "TestTopic")})
    @GetMapping(value = "/consumer")
    public Object queryConsumerByTopic(@RequestParam String topic) {
        return rmqConsumerManager.queryConsumeStatsListByTopicName(topic);
    }
    
    @Operation(summary = "查询主题的消费者组信息", description = "根据主题名查询该主题的消费者组订阅情况", tags = {
            "Topic"}, parameters = {
            @Parameter(name = "topic", description = "主题名称", required = true, example = "TestTopic")})
    @GetMapping(value = "/consumerInfo")
    public Object queryTopicConsumerInfo(@RequestParam String topic) {
        return rmqTopicManager.queryTopicConsumerInfo(topic);
    }
    
    @Operation(summary = "创建或更新主题配置", description = "根据传入的主题配置信息创建或更新主题", tags = {
            "Topic"}, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "主题配置信息", required = true, content = @Content(schema = @Schema(implementation = TopicConfigInfo.class))))
    @ApiResponse(responseCode = "200", description = "操作成功")
    @PostMapping
    public Object topicCreateOrUpdateRequest(@RequestBody TopicConfigInfo topicCreateOrUpdateRequest) {
        Preconditions.checkArgument(
                CommonUtils.isNotEmpty(topicCreateOrUpdateRequest.getBrokerNameList()) || CommonUtils.isNotEmpty(
                        topicCreateOrUpdateRequest.getClusterNameList()),
                "clusterName or brokerName can not be all blank");
        log.info("op=look topicCreateOrUpdateRequest={}", JsonUtil.obj2String(topicCreateOrUpdateRequest));
        rmqTopicManager.createOrUpdate(topicCreateOrUpdateRequest);
        return true;
    }
    
    @Operation(summary = "查看主题配置详情", description = "查看指定主题在 Broker 上的详细配置", tags = {
            "Topic"}, parameters = {
            @Parameter(name = "topic", description = "主题名称", required = true, example = "TestTopic"),
            @Parameter(name = "brokerName", description = "Broker 名称", required = false, example = "BrokerA")})
    @GetMapping(value = "/config")
    public Object examineTopicConfig(@RequestParam String topic, @RequestParam(required = false) String brokerName) {
        return rmqTopicManager.examineTopicConfig(topic);
    }
    
    @Operation(summary = "发送消息到主题", description = "向指定主题发送测试消息", tags = {
            "Topic"}, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "发送消息请求体", required = true, content = @Content(schema = @Schema(implementation = SendTopicMessageRequest.class))))
    @ApiResponse(responseCode = "200", description = "消息发送成功")
    @PostMapping(value = "/sendMessage")
    public Object sendTopicMessage(@RequestBody SendTopicMessageRequest sendTopicMessageRequest) {
        return rmqTopicManager.sendTopicMessageRequest(sendTopicMessageRequest);
    }
    
    
    @Operation(summary = "删除主题", description = "从集群中删除指定的主题", tags = {"Topic"}, parameters = {
            @Parameter(name = "clusterName", description = "集群名称（可选）", example = "DefaultCluster"),
            @Parameter(name = "topic", description = "主题名称", required = true, example = "TestTopic")})
    @DeleteMapping
    public Object delete(@RequestParam(required = false) String clusterName, @RequestParam String topic) {
        return rmqTopicManager.deleteTopic(topic, clusterName);
    }
    
    @Operation(summary = "从指定 Broker 删除主题", description = "在指定 Broker 上删除某个主题", tags = {
            "Topic"}, parameters = {
            @Parameter(name = "brokerName", description = "Broker 名称", required = true, example = "BrokerA"),
            @Parameter(name = "topic", description = "主题名称", required = true, example = "TestTopic")})
    @PostMapping("/deleteTopicByBroker")
    public Object deleteTopicByBroker(@RequestParam String brokerName, @RequestParam String topic) {
        return rmqTopicManager.deleteTopicInBroker(brokerName, topic);
    }
    
}
