package com.ch.cloud.kafka.mapper;

import com.ch.cloud.kafka.model.KafkaContentRecord;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

public interface KafkaContentRecordMapper extends Mapper<KafkaContentRecord>, InsertListMapper<KafkaContentRecord> {
}