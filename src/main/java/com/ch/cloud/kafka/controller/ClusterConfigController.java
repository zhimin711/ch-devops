package com.ch.cloud.kafka.controller;

import com.ch.StatusS;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.model.BtTopic;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.ITopicService;
import com.ch.cloud.kafka.tools.KafkaClusterUtils;
import com.ch.cloud.kafka.utils.ContextUtil;
import com.ch.e.PubError;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.DateUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author zhimin.ma
 * @date 2018/9/25 20:29
 */
@Api(tags = "KAFKA集群配置模块")
@RestController
@RequestMapping("cluster")
public class ClusterConfigController {

    @Autowired
    private ClusterConfigService clusterConfigService;
    @Autowired
    private ITopicService topicService;

    @GetMapping(value = {"{num}/{size}"})
    public PageResult<BtClusterConfig> page(BtClusterConfig record,
                                            @PathVariable(value = "num") int pageNum,
                                            @PathVariable(value = "size") int pageSize) {
        PageInfo<BtClusterConfig> pageInfo = clusterConfigService.findPage(record, pageNum, pageSize);
        return PageResult.success(pageInfo.getTotal(), pageInfo.getList());
    }

    @ApiOperation(value = "新增Kafka集群", notes = "")
    @PostMapping
    public Result<Integer> add(@RequestBody BtClusterConfig record) {
        BtClusterConfig r = clusterConfigService.findByClusterName(record.getClusterName());
        if (r != null) {
            return Result.error(PubError.EXISTS);
        }
        record.setStatus(StatusS.ENABLED);
        record.setCreateBy(ContextUtil.getUser());
        return ResultUtils.wrapFail(() -> clusterConfigService.save(record));
    }

    @PutMapping({"{id}"})
    public Result<Integer> edit(@PathVariable Long id, @RequestBody BtClusterConfig record) {
        return ResultUtils.wrapFail(() -> {
            record.setUpdateBy(ContextUtil.getUser());
            record.setUpdateAt(DateUtils.current());
            return clusterConfigService.update(record);
        });
    }

    @DeleteMapping({"{id}"})
    public Result<Integer> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            BtClusterConfig c = clusterConfigService.find(id);

            BtTopic srcRecord = new BtTopic();
            srcRecord.setClusterName(c.getClusterName());
            srcRecord.setStatus(StatusS.ENABLED);
            BtTopic targetRecord = new BtTopic();
            targetRecord.setStatus(StatusS.DELETE);
            targetRecord.setUpdateBy(ContextUtil.getUser());
            targetRecord.setUpdateAt(DateUtils.current());
            int c2 = topicService.update(srcRecord, targetRecord);
            return clusterConfigService.delete(id);
        });
    }


    @GetMapping("{id}")
    public Result<BtClusterConfig> detail(@PathVariable Long id){
        return ResultUtils.wrapFail(() -> {
            BtClusterConfig config = clusterConfigService.find(id);
            Set<String> topicNames = KafkaClusterUtils.fetchTopicNames(config);
            config.setTopicCount(topicNames.size());
            config.setBrokerCount(KafkaClusterUtils.countBroker(config));
            config.setConsumerCount(KafkaClusterUtils.countConsumerGroup(config));
            return config;
        });
    }
}
