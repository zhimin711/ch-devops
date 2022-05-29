package com.ch.cloud.kafka.service.impl;

import com.ch.StatusS;
import com.ch.cloud.kafka.mapper.KafkaClusterMapper;
import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.tools.KafkaClusterUtils;
import com.ch.mybatis.service.ServiceImpl;
import com.ch.utils.CommonUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author zhimin.ma
 * @since 2018/9/25 19:14
 */
@Service
public class KafkaClusterServiceImpl extends ServiceImpl<KafkaClusterMapper, KafkaCluster> implements KafkaClusterService {


    @Override
    public int save(KafkaCluster record) {
        fetchBrokers(record);
        return super.save(record);
    }

    private void fetchBrokers(KafkaCluster record) {
        Map<String, Integer> brokers = KafkaClusterUtils.getAllBrokersInCluster(record.getZookeeper());
        if (!brokers.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            brokers.forEach((k, v) -> {
                if (sb.length() > 0) sb.append(",");
                sb.append(k).append(":").append(v);
            });
            record.setBrokers(sb.toString());
        }
    }

    @Override
    public int update(KafkaCluster record) {
        fetchBrokers(record);
        return super.update(record);
    }

    @Override
    public KafkaCluster findByClusterName(String cluster) {
        if (CommonUtils.isEmpty(cluster)) {
            return null;
        }
        KafkaCluster q = new KafkaCluster();
        q.setClusterName(cluster);
        return getMapper().selectOne(q);
    }

    @Override
    public List<KafkaCluster> findEnabled() {
        KafkaCluster q = new KafkaCluster();
        q.setStatus(StatusS.ENABLED);
        return getMapper().select(q);
    }
}
