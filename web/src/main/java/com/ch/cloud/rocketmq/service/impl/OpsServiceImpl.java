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

package com.ch.cloud.rocketmq.service.impl;

import com.ch.cloud.rocketmq.config.RMQConfigure;
import com.ch.cloud.rocketmq.service.AbstractCommonService;
import com.ch.cloud.rocketmq.service.OpsService;
import com.ch.cloud.rocketmq.service.checker.CheckerType;
import com.ch.cloud.rocketmq.service.checker.RocketMqChecker;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OpsServiceImpl extends AbstractCommonService implements OpsService {
    
    @Resource
    private RMQConfigure configure;
    
    @Resource
    private List<RocketMqChecker> rocketMqCheckerList;
    
    @Override
    public List<RMQConfigure.Client> listNameSrv() {
        return configure.getClients();
    }
    
    @Override
    public RMQConfigure.Client getClient(String nameSrvAddr) {
        Assert.notEmpty(configure.getClients(), PubError.CONFIG, "nameSrv client");
        Optional<RMQConfigure.Client> first = configure.getClients().stream()
                .filter(client -> client.getAddr().equals(nameSrvAddr)).findFirst();
        return first.orElseGet(() -> configure.getClients().get(0));
    }
    
    @Override
    public Map<String, Object> homePageInfo() {
        Map<String, Object> homePageInfoMap = Maps.newHashMap();
        homePageInfoMap.put("namesrvAddrList", Splitter.on(";").splitToList(configure.getAddr()));
        return homePageInfoMap;
    }
    
    @Override
    public void updateNameSrvAddrList(String nameSrvAddrList) {
        configure.setAddr(nameSrvAddrList);
    }
    
    @Override
    public String getNameSrvList() {
        return configure.getAddr();
    }
    
    @Override
    public Map<CheckerType, Object> rocketMqStatusCheck() {
        Map<CheckerType, Object> checkResultMap = Maps.newHashMap();
        for (RocketMqChecker rocketMqChecker : rocketMqCheckerList) {
            checkResultMap.put(rocketMqChecker.checkerType(), rocketMqChecker.doCheck());
        }
        return checkResultMap;
    }
}
