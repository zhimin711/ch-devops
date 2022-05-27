package com.ch.cloud.devops.service;

import com.ch.mybatis.service.IService;
import com.ch.cloud.devops.domain.NamespaceApplyRecord;

/**
 * 申请空间记录Service接口
 *
 * @author admin
 * @since 2022-05-21 11:52:27
 */
public interface INamespaceApplyRecordService extends IService<NamespaceApplyRecord> {
    int approveNacos(NamespaceApplyRecord record);
}