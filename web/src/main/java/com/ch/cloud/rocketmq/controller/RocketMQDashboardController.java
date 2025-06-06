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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ch.cloud.rocketmq.controller;

import com.ch.cloud.rocketmq.config.RMQConfigure;
import com.ch.cloud.rocketmq.service.DashboardService;
import com.ch.cloud.rocketmq.service.OpsService;
import com.ch.utils.DateUtils;
import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "Rocket MQ 监控模块")
@RestController
@RequestMapping("/rocketmq/dashboard")
public class RocketMQDashboardController {
    
    @Resource
    DashboardService dashboardService;
    
    @Resource
    private OpsService opsService;
    
    @GetMapping(value = "/broker")
    public Object broker(@RequestParam String date, @CookieValue String nameSvrAddr) {
        
        RMQConfigure.Client client = opsService.getClient(nameSvrAddr);
        return dashboardService.listBrokerCollectData(client.getAddr(), DateUtils.parse(date));
    }
    
    @GetMapping(value = "/topic")
    public Object topic(@RequestParam String date, @RequestParam String topicName, @CookieValue String nameSvrAddr) {
        if (Strings.isNullOrEmpty(topicName)) {
            return null;
        }
        RMQConfigure.Client client = opsService.getClient(nameSvrAddr);
        return dashboardService.listTopicCollectData(client.getAddr(), topicName, DateUtils.parse(date));
    }
    
    @GetMapping(value = "/topicCurrent")
    public Object topicCurrent(@CookieValue String nameSvrAddr) {
        RMQConfigure.Client client = opsService.getClient(nameSvrAddr);
        return dashboardService.listLastTopicCollect(client.getAddr());
    }
    
}
