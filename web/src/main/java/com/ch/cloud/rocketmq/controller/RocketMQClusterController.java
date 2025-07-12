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

import com.ch.cloud.rocketmq.manager.RMQClusterManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rocket MQ 集群管理模块")
@RestController
@RequestMapping("/rocketmq/cluster")
public class RocketMQClusterController {

    @Autowired
    private RMQClusterManager rmqClusterManager;
    
    @GetMapping
    public Object list() {
        return rmqClusterManager.list();
    }

    @Operation(summary = "获取broker配置信息", description = "获取broker配置信息",tags = {"Rocket MQ 集群管理模块"})
    @GetMapping(value = "/brokerConfig")
    public Object brokerConfig(@RequestParam String brokerAddr) {
        return rmqClusterManager.getBrokerConfig(brokerAddr);
    }
}
