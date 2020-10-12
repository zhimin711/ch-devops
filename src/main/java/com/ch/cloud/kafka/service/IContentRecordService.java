package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.BtContentRecord;
import com.ch.mybatis.service.IService;

import java.util.List;

/**
 * decs:
 *
 * @author zhimin.ma
 * @date 2019/10/30
 */
public interface IContentRecordService extends IService<Long, BtContentRecord> {

    List<BtContentRecord> findBySid(Long sid);
}
