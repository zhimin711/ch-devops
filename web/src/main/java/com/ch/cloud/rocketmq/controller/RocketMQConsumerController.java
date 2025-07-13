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

import com.ch.cloud.rocketmq.manager.RMQConsumerManager;
import com.ch.cloud.rocketmq.model.ConnectionInfo;
import com.ch.cloud.rocketmq.model.GroupConsumeInfo;
import com.ch.cloud.rocketmq.model.request.ConsumerConfigInfo;
import com.ch.cloud.rocketmq.model.request.DeleteSubGroupRequest;
import com.ch.cloud.rocketmq.model.request.ResetOffsetRequest;
import com.ch.cloud.rocketmq.util.JsonUtil;
import com.ch.core.data.model.Page;
import com.ch.core.result.Result;
import com.ch.result.InvokerPage;
import com.ch.utils.CommonUtils;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Tag(name = "Rocket MQ 消费者管理模块")
@RestController
@RequestMapping("/rocketmq/consumer")
@Slf4j
public class RocketMQConsumerController {
    
    @Resource
    private RMQConsumerManager consumerManager;

    @GetMapping(value = "/groups")
    public Result<Page<GroupConsumeInfo>> list(@RequestParam Integer page, @RequestParam Integer pageSize, @RequestParam(required = false) String name) {
        return Result.from(() -> consumerManager.queryGroupList(page, pageSize, name));
    }

    @GetMapping(value = "/group")
    public Object groupQuery(@RequestParam String consumerGroup) {
        return consumerManager.queryGroup(consumerGroup);
    }

    @PostMapping(value = "/resetOffset")
    public Object resetOffset(@RequestBody ResetOffsetRequest resetOffsetRequest) {
        log.info("op=look resetOffsetRequest={}", JsonUtil.obj2String(resetOffsetRequest));
        return consumerManager.resetOffset(resetOffsetRequest);
    }

    @GetMapping(value = "/examineSubscriptionGroupConfig")
    public Object examineSubscriptionGroupConfig(@RequestParam String consumerGroup) {
        return consumerManager.examineSubscriptionGroupConfig(consumerGroup);
    }

    @PostMapping(value = "/deleteSubGroup")
    public Object deleteSubGroup(@RequestBody DeleteSubGroupRequest deleteSubGroupRequest) {
        return consumerManager.deleteSubGroup(deleteSubGroupRequest);
    }

    @PostMapping
    public Object consumerCreateOrUpdateRequest(@RequestBody ConsumerConfigInfo consumerConfigInfo) {
        Preconditions.checkArgument(CommonUtils.isNotEmpty(consumerConfigInfo.getBrokerNameList()) || CommonUtils.isNotEmpty(consumerConfigInfo.getClusterNameList()),
                "clusterName or brokerName can not be all blank");
        return consumerManager.createAndUpdateSubscriptionGroupConfig(consumerConfigInfo);
    }

    @GetMapping(value = "/fetchBrokerNameList")
    public Object fetchBrokerNameList(@RequestParam String consumerGroup) {
        return consumerManager.fetchBrokerNameSetBySubscriptionGroup(consumerGroup);
    }

    @GetMapping(value = "/queryTopic")
    public Object queryConsumerByTopic(@RequestParam String consumerGroup) {
        return consumerManager.queryConsumeStatsListByGroupName(consumerGroup);
    }

    @GetMapping(value = "/connection")
    public Object consumerConnection(@RequestParam(required = false) String consumerGroup) {
        ConsumerConnection consumerConnection = consumerManager.getConsumerConnection(consumerGroup);
        consumerConnection.setConnectionSet(ConnectionInfo.buildConnectionInfoHashSet(consumerConnection.getConnectionSet()));
        return consumerConnection;
    }

    @GetMapping(value = "/runningInfo")
    public Object getConsumerRunningInfo(@RequestParam String consumerGroup, @RequestParam String clientId,
                                         @RequestParam boolean jstack) {
        return consumerManager.getConsumerRunningInfo(consumerGroup, clientId, jstack);
    }
}
