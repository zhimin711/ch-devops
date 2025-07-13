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

package com.ch.cloud.rocketmq.manager;

import com.ch.cloud.rocketmq.model.MessageView;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.body.ConsumeMessageDirectlyResult;
import org.apache.rocketmq.tools.admin.api.MessageTrack;

import java.util.List;

public interface RMQMessageManager {
    /**
     * @param subject
     * @param msgId
     * @return
     */
    Pair<MessageView, List<MessageTrack>> viewMessage(String subject, final String msgId) throws Exception;

    List<MessageView> queryMessageByTopicAndKey(final String topic, final String key);

    /**
     * @param topic
     * @param begin
     * @param end
     * @return
     * @see org.apache.rocketmq.tools.command.message.PrintMessageSubCommand
     */
    List<MessageView> queryMessageByTopic(final String topic, final long begin,
                                          final long end);

    List<MessageTrack> messageTrackDetail(MessageExt msg);

    ConsumeMessageDirectlyResult consumeMessageDirectly(String topic, String msgId, String consumerGroup,
                                                        String clientId);

    @Deprecated // use viewMessage(String subject, final String msgId) instead
    Pair<MessageView, List<MessageTrack>> viewMessageByBrokerAndOffset(String brokerHost, int port, long offset) throws Exception;
}
