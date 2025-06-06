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

package com.ch.cloud.rocketmq.config;

import com.google.common.base.Strings;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.common.MixAll;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "rocketmq.namesrv")
@Slf4j
public class RMQConfigure {
    
    //use rocketmq.namesrv.addr first,if it is empty,than use system proerty or system env
    @Getter
    private volatile String addr = System.getProperty(MixAll.NAMESRV_ADDR_PROPERTY,
            System.getenv(MixAll.NAMESRV_ADDR_ENV));
    
    private String isVIPChannel;
    
    private String consoleCollectData;
    
    @Setter
    @Getter
    private List<Client> clients;
    
    public void setAddr(String addr) {
        if (StringUtils.isNotBlank(addr)) {
            this.addr = addr;
            System.setProperty(MixAll.NAMESRV_ADDR_PROPERTY, addr);
            log.info("setNameSrvAddrByProperty nameSrvAddr={}", addr);
        }
    }
    
    public String getConsoleCollectData() {
        if (!Strings.isNullOrEmpty(consoleCollectData)) {
            return consoleCollectData.trim();
        }
        return consoleCollectData;
    }
    
    public void setConsoleCollectData(String consoleCollectData) {
        this.consoleCollectData = consoleCollectData;
        if (!Strings.isNullOrEmpty(consoleCollectData)) {
            log.info("setConsoleCollectData consoleCollectData={}", consoleCollectData);
        }
    }
    
    public void setIsVIPChannel(String isVIPChannel) {
        if (StringUtils.isNotBlank(isVIPChannel)) {
            this.isVIPChannel = isVIPChannel;
            System.setProperty(ClientConfig.SEND_MESSAGE_WITH_VIP_CHANNEL_PROPERTY, isVIPChannel);
            log.info("setIsVIPChannel isVIPChannel={}", isVIPChannel);
        }
        if (StringUtils.isBlank(this.isVIPChannel)) {
            throw new IllegalArgumentException("======ERROR====== setIsVIPChannel is empty ======ERROR====== ");
        }
    }
    
    @Data
    public static class Client {
        
        private String name;
        
        private String addr;
        
        private Boolean enableCollect;
    }
}
