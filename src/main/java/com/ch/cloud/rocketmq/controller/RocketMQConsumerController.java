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

import com.ch.cloud.rocketmq.model.ConnectionInfo;
import com.ch.cloud.rocketmq.model.request.ConsumerConfigInfo;
import com.ch.cloud.rocketmq.model.request.DeleteSubGroupRequest;
import com.ch.cloud.rocketmq.model.request.ResetOffsetRequest;
import com.ch.cloud.rocketmq.service.ConsumerService;
import com.ch.cloud.rocketmq.util.JsonUtil;
import com.ch.utils.CommonUtils;
import com.google.common.base.Preconditions;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rocketmq/consumer")
public class RocketMQConsumerController {
    private Logger logger = LoggerFactory.getLogger(RocketMQConsumerController.class);

    @Resource
    private ConsumerService consumerService;

    @GetMapping(value = "/groups")
    public Object list() {
        return consumerService.queryGroupList();
    }

    @GetMapping(value = "/group")
    public Object groupQuery(@RequestParam String consumerGroup) {
        return consumerService.queryGroup(consumerGroup);
    }

    @RequestMapping(value = "/resetOffset.do", method = {RequestMethod.POST})
    public Object resetOffset(@RequestBody ResetOffsetRequest resetOffsetRequest) {
        logger.info("op=look resetOffsetRequest={}", JsonUtil.obj2String(resetOffsetRequest));
        return consumerService.resetOffset(resetOffsetRequest);
    }

    @GetMapping(value = "/examineSubscriptionGroupConfig")
    public Object examineSubscriptionGroupConfig(@RequestParam String consumerGroup) {
        return consumerService.examineSubscriptionGroupConfig(consumerGroup);
    }

    @RequestMapping(value = "/deleteSubGroup.do", method = {RequestMethod.POST})
    @ResponseBody
    public Object deleteSubGroup(@RequestBody DeleteSubGroupRequest deleteSubGroupRequest) {
        return consumerService.deleteSubGroup(deleteSubGroupRequest);
    }

    @RequestMapping(value = "/createOrUpdate.do", method = {RequestMethod.POST})
    public Object consumerCreateOrUpdateRequest(@RequestBody ConsumerConfigInfo consumerConfigInfo) {
        Preconditions.checkArgument(CommonUtils.isNotEmpty(consumerConfigInfo.getBrokerNameList()) || CommonUtils.isNotEmpty(consumerConfigInfo.getClusterNameList()),
                "clusterName or brokerName can not be all blank");
        return consumerService.createAndUpdateSubscriptionGroupConfig(consumerConfigInfo);
    }

    @GetMapping(value = "/fetchBrokerNameList.query")
    public Object fetchBrokerNameList(@RequestParam String consumerGroup) {
        return consumerService.fetchBrokerNameSetBySubscriptionGroup(consumerGroup);
    }

    @GetMapping(value = "/queryTopic")
    public Object queryConsumerByTopic(@RequestParam String consumerGroup) {
        return consumerService.queryConsumeStatsListByGroupName(consumerGroup);
    }

    @GetMapping(value = "/connection")
    public Object consumerConnection(@RequestParam(required = false) String consumerGroup) {
        ConsumerConnection consumerConnection = consumerService.getConsumerConnection(consumerGroup);
        consumerConnection.setConnectionSet(ConnectionInfo.buildConnectionInfoHashSet(consumerConnection.getConnectionSet()));
        return consumerConnection;
    }

    @GetMapping(value = "/runningInfo")
    public Object getConsumerRunningInfo(@RequestParam String consumerGroup, @RequestParam String clientId,
                                         @RequestParam boolean jstack) {
        return consumerService.getConsumerRunningInfo(consumerGroup, clientId, jstack);
    }
}
