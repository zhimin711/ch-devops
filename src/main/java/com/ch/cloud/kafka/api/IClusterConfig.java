package com.ch.cloud.kafka.api;

import com.ch.cloud.kafka.pojo.ClusterConfigInfo;
import com.ch.result.BaseResult;
import com.ch.result.PageResult;

/**
 * @author 01370603
 * @date 2018/9/25 18:19
 */
public interface IClusterConfig {

    BaseResult<Long> save(ClusterConfigInfo record);

    BaseResult<Long> update(ClusterConfigInfo record);

    PageResult<ClusterConfigInfo> findPageBy(int pageNum, int pageSize, ClusterConfigInfo record);

    BaseResult<ClusterConfigInfo> findListBy(ClusterConfigInfo record);
}
