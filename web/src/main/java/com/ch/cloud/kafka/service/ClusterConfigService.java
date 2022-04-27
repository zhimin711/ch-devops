package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.mybatis.service.IService;

import java.util.List;

/**
 * @author zhimin.ma
 * @date 2018/9/25 18:20
 */
public interface ClusterConfigService extends IService< BtClusterConfig> {

    BtClusterConfig findByClusterName(String cluster);

    List<BtClusterConfig> findEnabled();
}
