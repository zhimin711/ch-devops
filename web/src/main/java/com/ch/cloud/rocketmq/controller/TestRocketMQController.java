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

import com.ch.Separator;
import com.ch.cloud.rocketmq.config.RMQConfigure;
import com.ch.cloud.rocketmq.manager.RMQTopicManager;
import com.ch.cloud.rocketmq.util.JsonUtil;
import com.ch.e.ExUtils;
import com.ch.e.PubError;
import com.ch.utils.CommonUtils;
import com.ch.utils.StringUtilsV2;
import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Tag(name = "Rocket MQ 测试消息生产与消费模块")
@RestController
@RequestMapping("/test")
@Slf4j
public class TestRocketMQController {
    
    @Resource
    private RMQConfigure rMQConfigure;
    
    @Resource
    private RMQTopicManager rmqTopicManager;
    
    private Set<String> allTopics = Sets.newHashSet();
    
    private Set<String> existsTopics = Sets.newHashSet();
    
    @GetMapping(value = "/runTask")
    public Object list(@RequestParam String topicStr, @RequestParam(required = false) Integer consumerThreadSize,
            @RequestParam(required = false) Integer producerThreadSize) throws MQClientException {
        
        if (CommonUtils.isEmpty(topicStr)) {
            ExUtils.throwError(PubError.NON_NULL, "topic must be not null!");
        }
        if (CommonUtils.isEmpty(allTopics)) {
            TopicList list = rmqTopicManager.fetchAllTopicList();
            allTopics = list.getTopicList();
        }
        List<String> topics = StringUtilsV2.splitStr(Separator.S2, topicStr);
        Set<String> list = topics.stream().filter(e -> !existsTopics.contains(e) && allTopics.contains(e))
                .collect(Collectors.toSet());
        if (list.isEmpty()) {
            return null;
        }
        int consumerThreadSize2 = CommonUtils.isEmpty(consumerThreadSize) ? 1 : consumerThreadSize;
        int producerThreadSize2 = CommonUtils.isEmpty(producerThreadSize) ? 1 : producerThreadSize;
        for (String topic : list) {
            for (int i = 0; i < consumerThreadSize2; i++) {
                DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(topic + "_" + (i + 1) + "Group");
                consumer.setNamesrvAddr(rMQConfigure.getAddr());
                consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
                consumer.subscribe(topic, "*");
                consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                    log.info("{} receiveMessage msgSize={}", topic, msgs.size());
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                });
                consumer.start();
            }
            
            for (int j = 0; j < producerThreadSize2; j++) {
                final DefaultMQProducer producer = new DefaultMQProducer(topic + "_" + (j + 1) + "Group");
                producer.setInstanceName(String.valueOf(System.currentTimeMillis()));
                producer.setNamesrvAddr(rMQConfigure.getAddr());
                producer.start();
                new Thread(() -> {
                    int i = 0;
                    while (true) {
                        try {
                            Message msg = new Message(topic, "TagA" + i, "KEYS" + i,
                                    ("Hello RocketMQ " + i).getBytes());
                            Thread.sleep(1000L);
                            SendResult sendResult = producer.send(msg);
                            log.info("{} sendMessage={}", topic, JsonUtil.obj2String(sendResult));
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                Thread.sleep(1000);
                            } catch (Exception ignore) {
                            }
                        }
                    }
                }).start();
            }
            existsTopics.add(topic);
        }
        
        return true;
    }
}
