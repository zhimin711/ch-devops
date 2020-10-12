package com.ch.cloud.kafka.service.impl;

import com.ch.cloud.kafka.mapper.BtContentRecordMapper;
import com.ch.cloud.kafka.model.BtContentRecord;
import com.ch.cloud.kafka.service.IContentRecordService;
import com.ch.mybatis.service.BaseService;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * decs:
 *
 * @author zhimin.ma
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

    @Override
    public List<BtContentRecord> findBySid(Long sid) {
        if (sid == null) {
            return Lists.newArrayList();
        }
        BtContentRecord record = new BtContentRecord();
        record.setSid(sid);
        return btContentRecordMapper.select(record);
    }
}
