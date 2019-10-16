package com.ch.cloud.kafka.api;

import com.ch.cloud.kafka.pojo.TopicExtInfo;
import com.ch.result.PageResult;
import com.ch.result.Result;

/**
 * @author 01370603
 * @date 2018/9/25 18:19
 */
public interface TopicExtDS {

    Result<Long> save(TopicExtInfo record);

    Result<Long> update(TopicExtInfo record);

    PageResult<TopicExtInfo> findPageBy(int pageNum, int pageSize, TopicExtInfo record);

    Result<TopicExtInfo> findListBy(TopicExtInfo record);

}
