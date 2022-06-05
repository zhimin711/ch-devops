package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.KafkaContentRecord;
import com.ch.mybatis.service.IService;

import java.util.List;

/**
 * decs:
 *
 * @author zhimin.ma
 * @since 2019/10/30
 */
public interface KafkaContentRecordService extends IService<KafkaContentRecord> {

    List<KafkaContentRecord> findBySid(Long sid);
}
