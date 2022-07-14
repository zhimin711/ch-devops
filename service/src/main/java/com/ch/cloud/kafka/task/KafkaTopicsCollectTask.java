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
package com.ch.cloud.kafka.task;

import com.ch.cloud.kafka.dto.KafkaTopicDTO;
import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.cloud.kafka.tools.KafkaClusterManager;
import com.ch.cloud.kafka.tools.KafkaClusterUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KafkaTopicsCollectTask {
    private Date                currentDate = new Date();
    @Resource
    private KafkaClusterManager kafkaClusterManager;
    @Resource
    private KafkaClusterService kafkaClusterService;
    @Resource
    private KafkaTopicService   kafkaTopicService;

    @Scheduled(cron = "30 0/1 * * * ?")
    public void collectTopic() {
//        Date date = new Date();
//        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            List<KafkaCluster> clusterList = kafkaClusterService.findEnabled();
            for (KafkaCluster cluster : clusterList) {
                Set<String> topicNames = KafkaClusterUtils.fetchTopicNames(cluster);
                if(topicNames.isEmpty()){
                    continue;
                }

                List<KafkaTopic> existsTopics = kafkaTopicService.findByClusterIdAndTopicNames(cluster.getId(), topicNames);
                List<String> names = existsTopics.stream().map(KafkaTopic::getTopicName).collect(Collectors.toList());

                Set<String> topics = topicNames.stream().filter(e->!names.contains(e)).collect(Collectors.toSet());
                List<KafkaTopicDTO> fetchTopics = kafkaClusterManager.topics(cluster, topics);
                kafkaTopicService.saveOrUpdate(fetchTopics, "admin");
            }

        } catch (Exception err) {
            throw Throwables.propagate(err);
        }
    }

}
