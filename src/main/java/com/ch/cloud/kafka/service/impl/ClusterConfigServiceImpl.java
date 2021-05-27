package com.ch.cloud.kafka.service.impl;

import com.ch.StatusS;
import com.ch.cloud.kafka.mapper.BtClusterConfigMapper;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.tools.KafkaManager;
import com.ch.mybatis.service.ServiceImpl;
import com.ch.utils.CommonUtils;
import com.ch.utils.JSONUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author zhimin.ma
 * @date 2018/9/25 19:14
 */
@Service
public class ClusterConfigServiceImpl extends ServiceImpl<BtClusterConfigMapper, BtClusterConfig> implements ClusterConfigService {


    @Override
    public int save(BtClusterConfig record) {
        Map<String, Integer> brokers = KafkaManager.getAllBrokersInCluster(record.getZookeeper());
        record.setBrokers(JSONUtils.toJson(brokers));
        return super.save(record);
    }

    @Override
    public int update(BtClusterConfig record) {
        Map<String, Integer> brokers = KafkaManager.getAllBrokersInCluster(record.getZookeeper());
        record.setBrokers(JSONUtils.toJson(brokers));
        return super.update(record);
    }

    @Override
    public BtClusterConfig findByClusterName(String cluster) {
        if (CommonUtils.isEmpty(cluster)) {
            return null;
        }
        BtClusterConfig q = new BtClusterConfig();
        q.setClusterName(cluster);
        return getMapper().selectOne(q);
    }

    @Override
    public List<BtClusterConfig> findEnabled() {
        BtClusterConfig q = new BtClusterConfig();
        q.setStatus(StatusS.ENABLED);
        return getMapper().select(q);
    }
}
