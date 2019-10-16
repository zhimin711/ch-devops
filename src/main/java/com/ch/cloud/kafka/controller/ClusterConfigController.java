package com.ch.cloud.kafka.controller;

import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.pojo.ClusterConfigInfo;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.tools.TopicManager;
import com.ch.result.InvokerPage;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 01370603
 * @date 2018/9/25 20:29
 */
@Api(tags = "KAFKA集群配置模块")
@RestController
@RequestMapping("topic")
public class ClusterConfigController {

    @Autowired
    private ClusterConfigService clusterConfigService;

    @ApiOperation(value = "新增扩展信息", notes = "")
    public Result<Long> save(ClusterConfigInfo record) {
        BtClusterConfig r = new BtClusterConfig();
        BeanUtils.copyProperties(record, r);
        clusterConfigService.save(r);
        return Result.success(r.getId());
    }

    public Result<Long> update(ClusterConfigInfo record) {
        BtClusterConfig r = new BtClusterConfig();
        BeanUtils.copyProperties(record, r);
        clusterConfigService.update(r);
        return Result.success(r.getId());
    }

    public PageResult<ClusterConfigInfo> findPageBy(int pageNum, int pageSize, ClusterConfigInfo record) {
        return ResultUtils.wrapPage(() -> {

            BtClusterConfig r = new BtClusterConfig();
            BeanUtils.copyProperties(record, r);
            PageInfo<BtClusterConfig> page = clusterConfigService.findPage(pageNum, pageSize, r);
            List<ClusterConfigInfo> records = page.getList().stream().map(e -> {
                ClusterConfigInfo info = new ClusterConfigInfo();
                BeanUtils.copyProperties(e, info);
                return info;
            }).collect(Collectors.toList());

            return new InvokerPage.Page<>(page.getTotal(), records);
        });
    }

    public Result<String> getTopics(ClusterConfigInfo record) {
        BtClusterConfig cluster = clusterConfigService.findByClusterName(record.getClusterName());
        List<String> list = TopicManager.getAllTopics(cluster.getZookeeper());
        return Result.success(list);
    }
}
