package com.ch.cloud.kafka.service;

import com.ch.cloud.kafka.model.BtContentRecord;
import com.ch.mybatis.service.IService;

import java.util.List;

/**
 * decs:
 *
 * @author zhimin.ma
 * @since 2019/10/30
 */
public interface IContentRecordService extends IService<BtContentRecord> {

    List<BtContentRecord> findBySid(Long sid);
}
