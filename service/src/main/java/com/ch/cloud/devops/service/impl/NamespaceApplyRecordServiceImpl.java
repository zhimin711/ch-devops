package com.ch.cloud.devops.service.impl;

import com.ch.cloud.devops.domain.NamespaceApplyRecord;
import com.ch.cloud.devops.mapper.NamespaceApplyRecordMapper;
import com.ch.cloud.devops.service.INamespaceApplyRecordService;
import com.ch.mybatis.service.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 申请空间记录Service业务层处理
 *
 * @author admin
 * @since 2022-05-21 11:52:27
 */
@Service
public class NamespaceApplyRecordServiceImpl extends ServiceImpl<NamespaceApplyRecordMapper, NamespaceApplyRecord>
        implements INamespaceApplyRecordService {
    
}