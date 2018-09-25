package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.mybatis.service.IService;

/**
 * @author 01370603
 * @date 2018/9/25 18:20
 */
public interface ClusterConfigService extends IService<Long, BtClusterConfig> {

    BtClusterConfig findByClusterName(String cluster);
}
