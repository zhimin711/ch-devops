package com.ch.cloud.kafka.api;

import com.ch.cloud.kafka.pojo.TopicExtInfo;
import com.ch.result.Result;

/**
 * @author 01370603
 * @date 2018/9/25 10:02
 */
public interface IContentSearch {

    Result<String> search(TopicExtInfo record);
}
