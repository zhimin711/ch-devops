package com.ch.cloud.kafka.mapper;

import com.ch.cloud.kafka.model.BtContentRecord;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

public interface BtContentRecordMapper extends Mapper<BtContentRecord>, InsertListMapper<BtContentRecord> {
}