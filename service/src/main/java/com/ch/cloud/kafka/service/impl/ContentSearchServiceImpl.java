package com.ch.cloud.kafka.service.impl;

import com.ch.cloud.kafka.mapper.ContentSearchMapper;
import com.ch.cloud.kafka.model.ContentSearch;
import com.ch.cloud.kafka.service.IContentSearchService;
import com.ch.mybatis.service.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * decs:
 *
 * @author zhimin.ma
 * @since 2019/10/30
 */
@Service
public class ContentSearchServiceImpl extends ServiceImpl<ContentSearchMapper, ContentSearch> implements IContentSearchService {

    @Override
    public int start(Long id) {
        return getMapper().start(id);
    }

    @Override
    public int end(Long id, String status) {
        return getMapper().end(id, status);
    }
}
