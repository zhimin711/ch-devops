package com.ch.cloud.kafka.service.impl;

import com.ch.cloud.kafka.mapper.BtContentSearchMapper;
import com.ch.cloud.kafka.model.BtContentSearch;
import com.ch.cloud.kafka.service.IContentSearchService;
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
public class ContentSearchServiceImpl extends BaseService<Long, BtContentSearch> implements IContentSearchService {

    @Resource
    private BtContentSearchMapper contentSearchMapper;

    @Override
    protected Mapper<BtContentSearch> getMapper() {
        return contentSearchMapper;
    }

    @Override
    public int start(Long id) {
        return 0;
    }

    @Override
    public int end(Long id, String status) {
        return 0;
    }
}
