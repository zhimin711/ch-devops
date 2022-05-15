package com.ch.cloud.kafka.service.impl;

import com.ch.cloud.kafka.mapper.BtContentRecordMapper;
import com.ch.cloud.kafka.model.BtContentRecord;
import com.ch.cloud.kafka.service.IContentRecordService;
import com.ch.mybatis.service.ServiceImpl;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * decs:
 *
 * @author zhimin.ma
 * @since 2019/10/30
 */
@Service
public class ContentRecordServiceImpl extends ServiceImpl<BtContentRecordMapper, BtContentRecord> implements IContentRecordService {

    @Override
    public List<BtContentRecord> findBySid(Long sid) {
        if (sid == null) {
            return Lists.newArrayList();
        }
        BtContentRecord record = new BtContentRecord();
        record.setSid(sid);
        return getMapper().select(record);
    }
}
