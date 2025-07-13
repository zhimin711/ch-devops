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

import com.ch.cloud.rocketmq.manager.RMQMessageManager;
import com.ch.cloud.rocketmq.model.MessageView;
import com.ch.cloud.rocketmq.util.JsonUtil;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Tag(name = "Rocket MQ 消息搜索模块")
@RestController
@RequestMapping("/rocketmq/message")
@Slf4j
public class RocketMQMessageController {

    @Resource
    private RMQMessageManager messageManager;

    @Operation(summary = "查看消息", description = "通过 topic 和 msgId 查看消息")
    @GetMapping(value = "/view")
    public Object viewMessage(@RequestParam(required = false) String topic, @RequestParam String msgId)
            throws Exception {
        Map<String, Object> messageViewMap = Maps.newHashMap();
        Pair<MessageView, List<MessageTrack>> messageViewListPair = messageManager.viewMessage(topic, msgId);
        if (messageViewListPair == null) {
            return null;
        }
        messageViewMap.put("messageView", messageViewListPair.getObject1());
        messageViewMap.put("messageTrackList", messageViewListPair.getObject2());
        return messageViewMap;
    }

    @Operation(summary = "按主题和键查询消息", description = "通过 topic 和 key 查询消息")
    @GetMapping(value = "/queryByTopicAndKey")
    public Object queryMessageByTopicAndKey(@RequestParam String topic, @RequestParam String key) {
        return messageManager.queryMessageByTopicAndKey(topic, key);
    }

    @Operation(summary = "按主题查询消息", description = "通过 topic 和时间范围查询消息")
    @GetMapping(value = "/queryByTopic")
    public Object queryMessageByTopic(@RequestParam String topic, @RequestParam long begin, @RequestParam long end) {
        return messageManager.queryMessageByTopic(topic, begin, end);
    }

    @Operation(summary = "按 Broker 和偏移量查看消息", description = "通过 Broker 和偏移量查看消息")
    @GetMapping(value = "/viewByBrokerAndOffset")
    @Deprecated
    public Object viewMessageByBrokerAndOffset(@RequestParam String brokerHost, @RequestParam int port,
            @RequestParam long offset) throws Exception {
        Map<String, Object> messageViewMap = Maps.newHashMap();
        Pair<MessageView, List<MessageTrack>> messageViewListPair = messageManager.viewMessageByBrokerAndOffset(
                brokerHost, port, offset);
        messageViewMap.put("messageView", messageViewListPair.getObject1());
        messageViewMap.put("messageTrackList", messageViewListPair.getObject2());
        return messageViewMap;
    }

    @Operation(summary = "直接消费消息", description = "通过 topic、consumerGroup 和 msgId 直接消费消息")
    @PostMapping(value = "/consumeMessageDirectly")
    public Object consumeMessageDirectly(@RequestParam String topic, @RequestParam String consumerGroup,
            @RequestParam String msgId, @RequestParam(required = false) String clientId) {
        log.info("msgId={} consumerGroup={} clientId={}", msgId, consumerGroup, clientId);
        ConsumeMessageDirectlyResult consumeMessageDirectlyResult = messageManager.consumeMessageDirectly(topic, msgId,
                consumerGroup, clientId);
        log.info("consumeMessageDirectlyResult={}", JsonUtil.obj2String(consumeMessageDirectlyResult));
        return consumeMessageDirectlyResult;
    }
}
