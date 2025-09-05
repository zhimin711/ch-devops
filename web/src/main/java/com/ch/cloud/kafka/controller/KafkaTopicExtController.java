package com.ch.cloud.kafka.controller;

import com.ch.StatusS;
import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.model.KafkaTopicExt;
import com.ch.cloud.kafka.model.KafkaTopicExtProp;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.service.KafkaTopicExtService;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.e.ExUtils;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.ContextUtil;
import com.ch.toolkit.UUIDGenerator;
import com.ch.utils.BeanUtilsV2;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author zhimin.ma
 * @since 2018/9/25 20:29
 */
@Tag(name = "KAFKA主题扩展信息配置模块")
@RestController
@RequestMapping("kafka/topic/ext")
@Slf4j
public class KafkaTopicExtController {

    @Autowired
    private KafkaTopicExtService kafkaTopicExtService;
    @Autowired
    private KafkaClusterService  kafkaClusterService;
    @Autowired
    private KafkaTopicService   kafkaTopicService;

    @Value("${fs.path.libs}")
    private String libsDir;

    @Operation(summary = "加载主题扩展信息加载主题扩展信息", description = "加载主题扩展信息")
    @GetMapping
    public Result<KafkaTopicExt> configs(KafkaTopicExt record) {
        return ResultUtils.wrapList(() -> kafkaTopicExtService.findByClusterIdAndTopicNameAndCreateBy(record.getClusterId(), record.getTopicName(), ContextUtil.getUsername()));
    }

    @Operation(summary = "加载主题扩展信息", description = "加载主题扩展信息")
    @GetMapping("{id:[0-9]+}")
    public Result<KafkaTopicExt> load(@PathVariable Long id,
                                      KafkaTopicExt record) {
        return ResultUtils.wrap(() -> {
            KafkaTopicExt record2 = kafkaTopicExtService.find(id);
            if (record2 == null) {
                record2 = new KafkaTopicExt();
                record2.setClusterId(record.getClusterId());
                record2.setTopicName(record.getTopicName());
                record2.setThreadSize(4);
                record2.setBatchSize(10);
            }
            loadTopicProps(record2);
            return record2;
        });
    }

    private void loadTopicProps(KafkaTopicExt record) {

        List<KafkaTopicExtProp> props = kafkaTopicExtService.findProps(record.getId());

        if (CommonUtils.isEmpty(props)) {
            KafkaTopic topicDto = kafkaTopicService.findByClusterIdAndTopicName(record.getClusterId(), record.getTopicName());
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

    private List<KafkaTopicExtProp> convert(Map<?, ?> map) {
        List<KafkaTopicExtProp> props = Lists.newArrayList();
        map.forEach((k, v) -> {
            KafkaTopicExtProp prop = new KafkaTopicExtProp();
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

    @Operation(summary = "新增主题扩展信息", description = "新增主题扩展信息")
    @PostMapping
    public Result<Long> save(@RequestBody KafkaTopicExt record) {

        return ResultUtils.wrapFail(() -> {
            if (CommonUtils.isEmptyOr(record.getId(), record.getTopicName())) {
                ExUtils.throwError(PubError.NON_NULL, "集群或主题不能为空！");
            }
            KafkaTopic topicDto = kafkaTopicService.findByClusterIdAndTopicName(record.getClusterId(), record.getTopicName());
            if (topicDto == null) {
                ExUtils.throwError(PubError.NOT_EXISTS, "集群+主题不存在！");
            }

            KafkaTopicExt r = kafkaTopicExtService.find(record.getId());

            if (r != null) {
                record.setId(r.getId());
                record.setUpdateBy(ContextUtil.getUsername());
                record.setUpdateAt(DateUtils.current());
            } else {
                record.setId(null);
                record.setCreateBy(ContextUtil.getUsername());
                record.setStatus(StatusS.ENABLED);
            }
            kafkaTopicExtService.save(record);
            return record.getId();
        });
    }

    @Operation(summary = "删除主题扩展信息", description = "")
    @DeleteMapping({"{id:[0-9]+}"})
    public Result<Integer> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            KafkaTopicExt record = new KafkaTopicExt();
            record.setId(id);
            record.setStatus(StatusS.DELETE);
            record.setUpdateBy(ContextUtil.getUsername());
            record.setUpdateAt(DateUtils.current());
            return kafkaTopicExtService.update(record);
        });
    }

}
