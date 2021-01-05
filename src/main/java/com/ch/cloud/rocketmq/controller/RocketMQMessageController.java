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

import com.ch.cloud.rocketmq.model.MessageView;
import com.ch.cloud.rocketmq.service.MessageService;
import com.ch.cloud.rocketmq.util.JsonUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rocketmq/message")
@Slf4j
public class RocketMQMessageController {
    @Resource
    private MessageService messageService;

    @GetMapping(value = "/view")
    public Object viewMessage(@RequestParam(required = false) String topic, @RequestParam String msgId)  throws Exception {
        Map<String, Object> messageViewMap = Maps.newHashMap();
        Pair<MessageView, List<MessageTrack>> messageViewListPair = messageService.viewMessage(topic, msgId);
        if (messageViewListPair == null) {
            return null;
        }
        messageViewMap.put("messageView", messageViewListPair.getObject1());
        messageViewMap.put("messageTrackList", messageViewListPair.getObject2());
        return messageViewMap;
    }

    @GetMapping(value = "/queryByTopicAndKey")
    public Object queryMessageByTopicAndKey(@RequestParam String topic, @RequestParam String key) {
        return messageService.queryMessageByTopicAndKey(topic, key);
    }

    @GetMapping(value = "/queryByTopic")
    public Object queryMessageByTopic(@RequestParam String topic, @RequestParam long begin,
                                      @RequestParam long end) {
        return messageService.queryMessageByTopic(topic, begin, end);
    }

    @GetMapping(value = "/viewByBrokerAndOffset")
    @Deprecated
    public Object viewMessageByBrokerAndOffset(@RequestParam String brokerHost, @RequestParam int port,
                                               @RequestParam long offset) throws Exception{
        Map<String, Object> messageViewMap = Maps.newHashMap();
        Pair<MessageView, List<MessageTrack>> messageViewListPair = messageService.viewMessageByBrokerAndOffset(brokerHost, port, offset);
        messageViewMap.put("messageView", messageViewListPair.getObject1());
        messageViewMap.put("messageTrackList", messageViewListPair.getObject2());
        return messageViewMap;
    }

    @PostMapping(value = "/consumeMessageDirectly")
    public Object consumeMessageDirectly(@RequestParam String topic, @RequestParam String consumerGroup,
                                         @RequestParam String msgId,
                                         @RequestParam(required = false) String clientId) {
        log.info("msgId={} consumerGroup={} clientId={}", msgId, consumerGroup, clientId);
        ConsumeMessageDirectlyResult consumeMessageDirectlyResult = messageService.consumeMessageDirectly(topic, msgId, consumerGroup, clientId);
        log.info("consumeMessageDirectlyResult={}", JsonUtil.obj2String(consumeMessageDirectlyResult));
        return consumeMessageDirectlyResult;
    }
}
