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

package com.ch.cloud.rocketmq.service.impl;

import com.ch.cloud.rocketmq.dto.RMQBrokerCollect;
import com.ch.cloud.rocketmq.dto.RMQTopicCollect;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import com.ch.cloud.rocketmq.service.DashboardCollectService;
import com.ch.cloud.rocketmq.service.DashboardService;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {
    
    @Resource
    private DashboardCollectService dashboardCollectService;
    
    /**
     * @param date format yyyy-MM-dd
     * @return
     */
    @Override
    public Map<String, List<String>> queryBrokerData(String date) {
        return dashboardCollectService.getBrokerCache(date);
    }
    
    @Override
    public Map<String, List<String>> queryTopicData(String date) {
        return dashboardCollectService.getTopicCache(date);
    }
    
    /**
     * @param date      format yyyy-MM-dd
     * @param topicName
     * @return
     */
    @Override
    public List<String> queryTopicData(String date, String topicName) {
        if (null != dashboardCollectService.getTopicCache(date)) {
            return dashboardCollectService.getTopicCache(date).get(topicName);
        }
        return null;
    }
    
    /**
     * @return
     */
    @Override
    public List<String> queryTopicCurrentData() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, List<String>> topicCache = dashboardCollectService.getTopicCache(format.format(date));
        List<String> result = Lists.newArrayList();
        for (Map.Entry<String, List<String>> entry : topicCache.entrySet()) {
            List<String> value = entry.getValue();
            result.add(entry.getKey() + "," + value.get(value.size() - 1).split(",")[4]);
        }
        return result;
    }
    
    @Override
    public List<String> listLastTopicCollect(String nameSrvAddr) {
        List<RMQTopicCollect> list = dashboardCollectService.listLastTopicCollect(nameSrvAddr);
        if (CommonUtils.isNotEmpty(list)) {
            return Lists.transform(list, input -> input.getTopic() + "," + input.getOutMsgCntToday());
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<String> listTopicCollectData(String nameSrvAddr, String topicName, Date date) {
        List<RMQTopicCollect> list = dashboardCollectService.listTopicCollectData(nameSrvAddr, topicName, date);
        
        if (CommonUtils.isNotEmpty(list)) {
            return Lists.transform(list,
                    input -> input.getCollectTime().getTime() + "," + input.getInTps() + "," + input.getInMsgCntToday()
                            + "," + input.getOutTps() + "," + input.getOutMsgCntToday());
        }
        return Collections.emptyList();
    }
    
    @Override
    public Map<String, List<String>> listBrokerCollectData(String nameSrvAddr, Date date) {
        List<RMQBrokerCollect> list = dashboardCollectService.listBrokerCollectData(nameSrvAddr, date);
        if (CommonUtils.isNotEmpty(list)) {
            Map<String, List<String>> result = Maps.newHashMap();
            list.stream().collect(Collectors.groupingBy(RMQBrokerCollect::getBroker)).forEach((k, v) -> {
                result.put(k, Lists.transform(v, input -> input.getCollectTime().getTime() + "," + input.getAverageTps()));
            });
            return result;
        }
        return Collections.emptyMap();
    }
}
