package com.ch.cloud.kafka.controller;

import com.ch.StatusS;
import com.ch.cloud.kafka.model.BtTopic;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.model.BtTopicExtProp;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.ITopicExtService;
import com.ch.cloud.kafka.service.ITopicService;
import com.ch.cloud.utils.ContextUtil;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.UUIDGenerator;
import com.ch.utils.BeanUtilsV2;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.ch.e.ExceptionUtils;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author zhimin.ma
 * @date 2018/9/25 20:29
 */
@Api(tags = "KAFKA主题扩展信息配置模块")
@RestController
@RequestMapping("topic/ext")
@Slf4j
public class KafkaTopicExtController {

    @Autowired
    private ITopicExtService topicExtService;
    @Autowired
    private ClusterConfigService clusterConfigService;
    @Autowired
    private ITopicService topicService;

    @Value("${share.path.libs}")
    private String libsDir;

    @ApiOperation(value = "加载主题扩展信息", notes = "加载主题扩展信息")
    @GetMapping
    public Result<BtTopicExt> configs(BtTopicExt record) {
        return ResultUtils.wrapList(() -> topicExtService.findByClusterAndTopicAndCreateBy(record.getClusterName(), record.getTopicName(), ContextUtil.getUser()));
    }

    @ApiOperation(value = "加载主题扩展信息", notes = "加载主题扩展信息")
    @GetMapping("{id}")
    public Result<BtTopicExt> load(@PathVariable Long id,
                                   BtTopicExt record) {
        return ResultUtils.wrap(() -> {
            BtTopicExt record2 = topicExtService.find(id);
            if (record2 == null) {
                record2 = new BtTopicExt();
                record2.setClusterName(record.getClusterName());
                record2.setTopicName(record.getTopicName());
                record2.setThreadSize(4);
                record2.setBatchSize(10);
            }
            loadTopicProps(record2);
            return record2;
        });
    }

    private void loadTopicProps(BtTopicExt record) {

        List<BtTopicExtProp> props = topicExtService.findProps(record.getId());

        if (CommonUtils.isEmpty(props)) {
            BtTopic topicDto = topicService.findByClusterAndTopic(record.getClusterName(), record.getTopicName());
            if (CommonUtils.isEmpty(topicDto.getClassName())) {
                return;
            }
            Class<?> clazz = KafkaSerializeUtils.loadClazz(libsDir + File.separator + topicDto.getClassFile(), topicDto.getClassName());
            if (clazz == null) {
                return;
            }
//            Object obj = clazz.newInstance();
            Map<String, Object> map = BeanUtilsV2.getPropertyAndType(clazz);
            props = convert(map);
        }

        record.setProps(props);
    }

    private List<BtTopicExtProp> convert(Map<?, ?> map) {
        List<BtTopicExtProp> props = Lists.newArrayList();
        map.forEach((k, v) -> {
            BtTopicExtProp prop = new BtTopicExtProp();
            prop.setUid(UUIDGenerator.generate());
            prop.setCode(k.toString());
            if (v != null) {
                if (v instanceof String) {
                    prop.setType((String) v);
                } else if (v instanceof Map) {
                    prop.setType("{}");
                    prop.setChildren(convert((Map) v));
                }
            }
            props.add(prop);
        });
        return props;
    }

    @ApiOperation(value = "新增主题扩展信息", notes = "新增主题扩展信息")
    @PostMapping
    public Result<Long> save(@RequestBody BtTopicExt record) {

        return ResultUtils.wrapFail(() -> {
            if (CommonUtils.isEmptyOr(record.getClusterName(), record.getTopicName())) {
                ExceptionUtils._throw(PubError.NON_NULL, "集群或主题不能为空！");
            }
            BtTopic topicDto = topicService.findByClusterAndTopic(record.getClusterName(), record.getTopicName());
            if (topicDto == null) {
                ExceptionUtils._throw(PubError.NOT_EXISTS, "集群+主题不存在！");
            }

            BtTopicExt r = topicExtService.find(record.getId());

            if (r != null) {
                record.setId(r.getId());
                record.setUpdateBy(ContextUtil.getUser());
                record.setUpdateAt(DateUtils.current());
            } else {
                record.setId(null);
                record.setCreateBy(ContextUtil.getUser());
                record.setStatus(StatusS.ENABLED);
            }
            topicExtService.save(record);
            return record.getId();
        });
    }

    @ApiOperation(value = "删除主题扩展信息", notes = "")
    @DeleteMapping({"{id}"})
    public Result<Integer> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            BtTopicExt record = new BtTopicExt();
            record.setId(id);
            record.setStatus(StatusS.DELETE);
            record.setUpdateBy(ContextUtil.getUser());
            record.setUpdateAt(DateUtils.current());
            return topicExtService.update(record);
        });
    }

}
