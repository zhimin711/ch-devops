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

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.rocketmq.config.RMQConfigure;
import com.ch.cloud.rocketmq.dto.RMQBrokerCollect;
import com.ch.cloud.rocketmq.dto.RMQTopicCollect;
import com.ch.cloud.rocketmq.exception.ServiceException;
import com.ch.cloud.rocketmq.mapper.RMQBrokerCollectMapper;
import com.ch.cloud.rocketmq.mapper.RMQTopicCollectMapper;
import com.ch.cloud.rocketmq.service.DashboardCollectService;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.Sqls;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class DashboardCollectServiceImpl implements DashboardCollectService {
    
    @Resource
    private RMQConfigure rmqConfigure;
    
    @Resource
    private RMQTopicCollectMapper topicCollectMapper;
    
    @Resource
    private RMQBrokerCollectMapper brokerCollectMapper;
    
    private LoadingCache<String, List<String>> brokerMap = CacheBuilder.newBuilder().maximumSize(1000)
            .concurrencyLevel(10).recordStats().ticker(Ticker.systemTicker()).removalListener(notification -> log.debug(
                    notification.getKey() + " was removed, cause is " + notification.getCause()))
            .build(new CacheLoader<String, List<String>>() {
                @Override
                public List<String> load(String key) {
                    return Lists.newArrayList();
                }
            });
    
    private LoadingCache<String, List<String>> topicMap = CacheBuilder.newBuilder().maximumSize(1000)
            .concurrencyLevel(10).recordStats().ticker(Ticker.systemTicker()).removalListener(notification -> log.debug(
                    notification.getKey() + " was removed, cause is " + notification.getCause()))
            .build(new CacheLoader<String, List<String>>() {
                @Override
                public List<String> load(String key) {
                    return Lists.newArrayList();
                }
            });
    
    @Override
    public LoadingCache<String, List<String>> getBrokerMap() {
        return brokerMap;
    }
    
    @Override
    public LoadingCache<String, List<String>> getTopicMap() {
        return topicMap;
    }
    
    @Override
    public Map<String, List<String>> jsonDataFile2map(File file) {
        List<String> strings;
        try {
            strings = Files.readLines(file, Charsets.UTF_8);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        StringBuffer sb = new StringBuffer();
        for (String string : strings) {
            sb.append(string);
        }
        JSONObject json = JSONObject.parseObject(sb.toString());
        Set<Map.Entry<String, Object>> entries = json.entrySet();
        Map<String, List<String>> map = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : entries) {
            JSONArray tpsArray = (JSONArray) entry.getValue();
            if (tpsArray == null) {
                continue;
            }
            Object[] tpsStrArray = tpsArray.toArray();
            List<String> tpsList = Lists.newArrayList();
            for (Object tpsObj : tpsStrArray) {
                tpsList.add("" + tpsObj);
            }
            map.put(entry.getKey(), tpsList);
        }
        return map;
    }
    
    @Override
    public Map<String, List<String>> getBrokerCache(String date) {
        String dataLocationPath = rmqConfigure.getConsoleCollectData();
        File file = new File(dataLocationPath + date + ".json");
        if (!file.exists()) {
            throw Throwables.propagate(new ServiceException(1, "This date have't data!"));
        }
        return jsonDataFile2map(file);
    }
    
    @Override
    public Map<String, List<String>> getTopicCache(String date) {
        String dataLocationPath = rmqConfigure.getConsoleCollectData();
        File file = new File(dataLocationPath + date + "_topic" + ".json");
        if (!file.exists()) {
            throw Throwables.propagate(new ServiceException(1, "This date have't data!"));
        }
        return jsonDataFile2map(file);
    }
    
    @Override
    public boolean saveBatchTopicData(List<RMQTopicCollect> list) {
        
        return topicCollectMapper.insertList(list) > 0;
    }
    
    @Override
    public List<RMQTopicCollect> listLastTopicCollect(String clientAddr) {
        List<Long> ids = topicCollectMapper.groupTopicLastCollectId(clientAddr, DateUtils.currentZeroTime());
        if (CommonUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return topicCollectMapper.selectByExample(
                Example.builder(RMQTopicCollect.class).andWhere(Sqls.custom().andIn("id", ids)).build());
    }
    
    @Override
    public List<RMQTopicCollect> listTopicCollectData(String nameSrvAddr, String topicName, Date date) {
        Example ex = Example.builder(RMQTopicCollect.class).andWhere(
                Sqls.custom().andEqualTo("nameSrvAddr", nameSrvAddr).andEqualTo("topic", topicName)
                        .andEqualTo("collectDate", date)).orderByAsc("collectTime").build();
        
        return topicCollectMapper.selectByExample(ex);
    }
    
    @Override
    public List<RMQBrokerCollect> listBrokerCollectData(String nameSrvAddr, Date date) {
        Example ex = Example.builder(RMQBrokerCollect.class)
                .andWhere(Sqls.custom().andEqualTo("nameSrvAddr", nameSrvAddr).andEqualTo("collectDate", date))
                .orderByAsc("collectTime").build();
        return brokerCollectMapper.selectByExample(ex);
    }
    
    @Override
    public boolean saveBatchBrokerData(List<RMQBrokerCollect> data) {
        return brokerCollectMapper.insertList(data) > 0;
    }
    
}
