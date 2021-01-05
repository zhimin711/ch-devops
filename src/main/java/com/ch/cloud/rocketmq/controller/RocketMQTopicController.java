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

import com.ch.cloud.rocketmq.model.request.SendTopicMessageRequest;
import com.ch.cloud.rocketmq.model.request.TopicConfigInfo;
import com.ch.cloud.rocketmq.service.ConsumerService;
import com.ch.cloud.rocketmq.service.TopicService;
import com.ch.cloud.rocketmq.util.JsonUtil;
import com.ch.utils.CommonUtils;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rocketmq/topic")
public class RocketMQTopicController {
    private Logger logger = LoggerFactory.getLogger(RocketMQTopicController.class);

    @Resource
    private TopicService topicService;

    @Resource
    private ConsumerService consumerService;

    @GetMapping
    public Object list() {
        return topicService.fetchAllTopicList();
    }

    @GetMapping(value = "/stats")
    public Object stats(@RequestParam String topic) {
        return topicService.stats(topic);
    }

    @RequestMapping(value = "/route", method = RequestMethod.GET)
    public Object route(@RequestParam String topic) {
        return topicService.route(topic);
    }

    @RequestMapping(value = "/consumer")
    public Object queryConsumerByTopic(@RequestParam String topic) {
        return consumerService.queryConsumeStatsListByTopicName(topic);
    }

    @GetMapping(value = "/consumerInfo")
    public Object queryTopicConsumerInfo(@RequestParam String topic) {
        return topicService.queryTopicConsumerInfo(topic);
    }

    @PostMapping
    public Object topicCreateOrUpdateRequest(@RequestBody TopicConfigInfo topicCreateOrUpdateRequest) {
        Preconditions.checkArgument(CommonUtils.isNotEmpty(topicCreateOrUpdateRequest.getBrokerNameList()) || CommonUtils.isNotEmpty(topicCreateOrUpdateRequest.getClusterNameList()),
                "clusterName or brokerName can not be all blank");
        logger.info("op=look topicCreateOrUpdateRequest={}", JsonUtil.obj2String(topicCreateOrUpdateRequest));
        topicService.createOrUpdate(topicCreateOrUpdateRequest);
        return true;
    }

    @GetMapping(value = "/config")
    public Object examineTopicConfig(@RequestParam String topic,
                                     @RequestParam(required = false) String brokerName) {
        return topicService.examineTopicConfig(topic);
    }

    @PostMapping(value = "/sendMessage")
    public Object sendTopicMessage(@RequestBody SendTopicMessageRequest sendTopicMessageRequest) {
        return topicService.sendTopicMessageRequest(sendTopicMessageRequest);
    }

    /**
     * @param clusterName
     * @param topic
     * @return
     */
    @DeleteMapping
    public Object delete(@RequestParam(required = false) String clusterName, @RequestParam String topic) {
        return topicService.deleteTopic(topic, clusterName);
    }

    @RequestMapping(value = "/deleteTopicByBroker.do", method = {RequestMethod.POST})
    public Object deleteTopicByBroker(@RequestParam String brokerName, @RequestParam String topic) {
        return topicService.deleteTopicInBroker(brokerName, topic);
    }

}
