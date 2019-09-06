package com.ch.cloud.kafka.api;

import com.ch.cloud.kafka.pojo.ClusterConfigInfo;
import com.ch.result.PageResult;
import com.ch.result.Result;

/**
 * @author 01370603
 * @date 2018/9/25 18:19
 */
public interface IClusterConfig {

    Result<Long> save(ClusterConfigInfo record);

    Result<Long> update(ClusterConfigInfo record);

    PageResult<ClusterConfigInfo> findPageBy(int pageNum, int pageSize, ClusterConfigInfo record);

    Result<ClusterConfigInfo> findListBy(ClusterConfigInfo record);

    Result<String> getTopics(ClusterConfigInfo record);
}
