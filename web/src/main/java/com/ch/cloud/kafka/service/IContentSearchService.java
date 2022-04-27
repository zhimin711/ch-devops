package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.BtContentSearch;
import com.ch.mybatis.service.IService;

/**
 * decs:
 *
 * @author zhimin.ma
 * @date 2019/10/30
 */
public interface IContentSearchService extends IService<BtContentSearch> {

    int start(Long id);

    int end(Long id, String status);
}
