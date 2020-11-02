package com.ch.cloud.kafka.service.impl;

import com.ch.Constants;
import com.ch.cloud.kafka.mapper.BtTopicExtMapper;
import com.ch.cloud.kafka.mapper.BtTopicExtPropMapper;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.model.BtTopicExtProp;
import com.ch.cloud.kafka.service.ITopicExtService;
import com.ch.mybatis.service.BaseService;
import com.ch.toolkit.UUIDGenerator;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhimin.ma
 * @date 2018/9/25 19:14
 */
@Service
public class TopicExtServiceImpl extends BaseService<Long, BtTopicExt> implements ITopicExtService {

    @Resource
    private BtTopicExtMapper mapper;

    @Override
    protected Mapper<BtTopicExt> getMapper() {
        return mapper;
    }

    @Resource
    private BtTopicExtPropMapper propMapper;

    @Override
    public BtTopicExt findByClusterAndTopicAndCreateBy(String clusterName, String topicName, String username) {
        if (CommonUtils.isEmptyOr(clusterName, topicName, username)) {
            return null;
        }
        BtTopicExt q = new BtTopicExt();
        q.setClusterName(clusterName);
        q.setTopicName(topicName);
        q.setCreateBy(username);
        return getMapper().selectOne(q);
    }

    @Override
    public List<BtTopicExtProp> findProps(Long id) {
        if (CommonUtils.isEmpty(id)) {
            return null;
        }
        BtTopicExtProp record2 = new BtTopicExtProp();
        record2.setMid(id);
        List<BtTopicExtProp> list = propMapper.select(record2);
        return convertPropTree(list);
    }

    private List<BtTopicExtProp> convertPropTree(List<BtTopicExtProp> list) {
        if (CommonUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        Map<Long, List<BtTopicExtProp>> propMap = list.stream().collect(Collectors.groupingBy(BtTopicExtProp::getPid));
        propMap.forEach((k, v) -> v.forEach(e -> e.setChildren(propMap.get(e.getId()))));

        List<BtTopicExtProp> topList = propMap.get(0L);
        topList.sort((r1, r2) -> CommonUtils.compareTo(r1.getSort(), r2.getSort()));
        return topList;
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public int save(BtTopicExt record) {
        int c;
        if (CommonUtils.isEmpty(record.getId())) {
            c = super.save(record);
        } else {
            c = super.update(record);
        }
        BtTopicExtProp record2 = new BtTopicExtProp();
        record2.setMid(record.getId());
        propMapper.delete(record2);
        if (c > 0 && CommonUtils.isNotEmpty(record.getProps())) {
            saveProps(record, record.getProps());
        }
        return c;
    }

    private void saveProps(BtTopicExt record, List<BtTopicExtProp> props) {
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
