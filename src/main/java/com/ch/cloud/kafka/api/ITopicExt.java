package com.ch.cloud.kafka.api;

import com.ch.cloud.kafka.pojo.TopicExtInfo;
import com.ch.result.BaseResult;
import com.ch.result.PageResult;

/**
 * @author 01370603
 * @date 2018/9/25 18:19
 */
public interface ITopicExt {

    BaseResult<Long> save(TopicExtInfo record);

    BaseResult<Long> update(TopicExtInfo record);

    PageResult<TopicExtInfo> findPageBy(int pageNum, int pageSize, TopicExtInfo record);

    BaseResult<TopicExtInfo> findListBy(TopicExtInfo record);

}
