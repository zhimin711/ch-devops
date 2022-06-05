package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.KafkaContentSearch;
import com.ch.mybatis.service.IService;

/**
 * decs:
 *
 * @author zhimin.ma
 * @since 2019/10/30
 */
public interface KafkaContentSearchService extends IService<KafkaContentSearch> {

    int start(Long id);

    int end(Long id, String status);
}
