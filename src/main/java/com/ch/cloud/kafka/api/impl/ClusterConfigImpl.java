package com.ch.cloud.kafka.api.impl;

import com.ch.cloud.kafka.api.IClusterConfig;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.pojo.ClusterConfigInfo;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.tools.KafkaManager;
import com.ch.cloud.kafka.tools.TopicManager;
import com.ch.result.BaseResult;
import com.ch.result.PageResult;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 01370603
 * @date 2018/9/25 20:29
 */
@Service
@com.alibaba.dubbo.config.annotation.Service
public class ClusterConfigImpl implements IClusterConfig {

    @Autowired
    private ClusterConfigService clusterConfigService;

    @Override
    public BaseResult<Long> save(ClusterConfigInfo record) {
        BtClusterConfig r = new BtClusterConfig();
        BeanUtils.copyProperties(record, r);
        clusterConfigService.save(r);
        return new BaseResult<>(r.getId());
    }

    @Override
    public BaseResult<Long> update(ClusterConfigInfo record) {
        BtClusterConfig r = new BtClusterConfig();
        BeanUtils.copyProperties(record, r);
        clusterConfigService.update(r);
        return new BaseResult<Long>(r.getId());
    }

    @Override
    public PageResult<ClusterConfigInfo> findPageBy(int pageNum, int pageSize, ClusterConfigInfo record) {
        BtClusterConfig r = new BtClusterConfig();
        BeanUtils.copyProperties(record, r);
        PageInfo<BtClusterConfig> page = clusterConfigService.findPage(r, pageNum, pageSize);
        List<ClusterConfigInfo> records = page.getList().stream().map(e -> {
            ClusterConfigInfo info = new ClusterConfigInfo();
            BeanUtils.copyProperties(e, info);
            return info;
        }).collect(Collectors.toList());
        return new PageResult<>(records, pageNum, pageSize, page.getTotal());
    }

    @Override
    public BaseResult<ClusterConfigInfo> findListBy(ClusterConfigInfo record) {
        BtClusterConfig r = new BtClusterConfig();
        BeanUtils.copyProperties(record, r);
        List<BtClusterConfig> list = clusterConfigService.find(r);
        List<ClusterConfigInfo> records = list.stream().map(e -> {
            ClusterConfigInfo info = new ClusterConfigInfo();
            BeanUtils.copyProperties(e, info);
            return info;
        }).collect(Collectors.toList());
        return new BaseResult<ClusterConfigInfo>(records);
    }

    @Override
    public BaseResult<String> getTopics(ClusterConfigInfo record) {
        BtClusterConfig cluster = clusterConfigService.findByClusterName(record.getClusterName());
        List<String> list = TopicManager.getAllTopics(cluster.getZookeeper());
        return new BaseResult<>(list);
    }
}
