package com.ch.cloud.kafka.service.impl;

import com.ch.Constants;
import com.ch.StatusS;
import com.ch.cloud.kafka.mapper.KafkaTopicExtMapper;
import com.ch.cloud.kafka.mapper.KafkaTopicExtPropMapper;
import com.ch.cloud.kafka.model.KafkaTopicExt;
import com.ch.cloud.kafka.model.KafkaTopicExtProp;
import com.ch.cloud.kafka.service.KafkaTopicExtService;
import com.ch.mybatis.service.ServiceImpl;
import com.ch.toolkit.UUIDGenerator;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.Sqls;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhimin.ma
 * @since 2018/9/25 19:14
 */
@Service
public class KafkaTopicExtServiceImpl extends ServiceImpl<KafkaTopicExtMapper, KafkaTopicExt> implements KafkaTopicExtService {

    @Resource
    private KafkaTopicExtPropMapper propMapper;

    @Override
    public List<KafkaTopicExt> findByClusterAndTopicAndCreateBy(String clusterName, String topicName, String username) {
        if (CommonUtils.isEmptyOr(clusterName, topicName, username)) {
            return null;
        }
        KafkaTopicExt q = new KafkaTopicExt();
        q.setClusterName(clusterName);
        q.setTopicName(topicName);
        q.setCreateBy(username);
        Example example = Example.builder(KafkaTopicExt.class).select("id", "description")
                .where(Sqls.custom()
                        .andEqualTo("clusterName", clusterName)
//                        .andEqualTo("createBy", username)
                        .andEqualTo("status", StatusS.ENABLED)
                        .andEqualTo("topicName", topicName))
                .build();
        return getMapper().selectByExample(example);
    }

    @Override
    public List<KafkaTopicExtProp> findProps(Long id) {
        if (CommonUtils.isEmpty(id)) {
            return null;
        }
        KafkaTopicExtProp record2 = new KafkaTopicExtProp();
        record2.setMid(id);
        List<KafkaTopicExtProp> list = propMapper.select(record2);
        return convertPropTree(list);
    }

    private List<KafkaTopicExtProp> convertPropTree(List<KafkaTopicExtProp> list) {
        if (CommonUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        Map<Long, List<KafkaTopicExtProp>> propMap = list.stream().collect(Collectors.groupingBy(KafkaTopicExtProp::getPid));
        propMap.forEach((k, v) -> v.forEach(e -> e.setChildren(propMap.get(e.getId()))));

        List<KafkaTopicExtProp> topList = propMap.get(0L);
        topList.sort((r1, r2) -> CommonUtils.compareTo(r1.getSort(), r2.getSort()));
        return topList;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int save(KafkaTopicExt record) {
        int c;
        if (CommonUtils.isEmpty(record.getId())) {
            c = super.save(record);
        } else {
            c = super.update(record);
        }
        KafkaTopicExtProp record2 = new KafkaTopicExtProp();
        record2.setMid(record.getId());
        propMapper.delete(record2);
        if (c > 0 && CommonUtils.isNotEmpty(record.getProps())) {
            saveProps(record, record.getProps());
        }
        return c;
    }

    private void saveProps(KafkaTopicExt record, List<KafkaTopicExtProp> props) {
        props.forEach(r -> {
            r.setMid(record.getId());
            if (CommonUtils.isEmpty(r.getPid())) {
                r.setPid(0L);
            }
            if (CommonUtils.isEmpty(r.getUid())) {
                r.setUid(UUIDGenerator.generate());
            }
            if (CommonUtils.isEmpty(r.getStatus())) {
                r.setStatus(Constants.ENABLED);
            }
            propMapper.insert(r);
            if (CommonUtils.isNotEmpty(r.getChildren())) {
                r.getChildren().forEach(e -> e.setPid(r.getId()));
                saveProps(record, r.getChildren());
            }
        });
    }
}
