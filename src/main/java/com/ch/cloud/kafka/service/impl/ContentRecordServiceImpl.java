package com.ch.cloud.kafka.service.impl;

import com.ch.cloud.kafka.mapper.BtContentRecordMapper;
import com.ch.cloud.kafka.model.BtContentRecord;
import com.ch.cloud.kafka.service.IContentRecordService;
import com.ch.mybatis.service.BaseService;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/10/30
 */
@Service
public class ContentRecordServiceImpl extends BaseService<Long, BtContentRecord> implements IContentRecordService {

    @Resource
    private BtContentRecordMapper btContentRecordMapper;

    @Override
    protected Mapper<BtContentRecord> getMapper() {
        return btContentRecordMapper;
    }
}
