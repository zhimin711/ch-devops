package com.ch.cloud.kafka.service.impl;

import com.ch.cloud.kafka.mapper.KafkaContentRecordMapper;
import com.ch.cloud.kafka.model.KafkaContentRecord;
import com.ch.cloud.kafka.service.KafkaContentRecordService;
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
public class KafkaContentRecordServiceImpl extends ServiceImpl<KafkaContentRecordMapper, KafkaContentRecord> implements KafkaContentRecordService {

    @Override
    public List<KafkaContentRecord> findBySid(Long sid) {
        if (sid == null) {
            return Lists.newArrayList();
        }
        KafkaContentRecord record = new KafkaContentRecord();
        record.setSid(sid);
        return getMapper().select(record);
    }
}
