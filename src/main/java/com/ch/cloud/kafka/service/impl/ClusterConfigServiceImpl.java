package com.ch.cloud.kafka.service.impl;

import com.ch.cloud.kafka.mapper.BtClusterConfigMapper;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.mybatis.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author 01370603
 * @date 2018/9/25 19:14
 */
@Service
public class ClusterConfigServiceImpl extends BaseService<Long, BtClusterConfig> implements ClusterConfigService {

    @Autowired(required = false)
    private BtClusterConfigMapper clusterConfigMapper;

    @Override
    protected Mapper<BtClusterConfig> getMapper() {
        return clusterConfigMapper;
    }

    @Override
    public BtClusterConfig findByClusterName(String cluster) {
        BtClusterConfig q = new BtClusterConfig();
        q.setClusterName(cluster);
        return clusterConfigMapper.selectOne(q);
    }
}
