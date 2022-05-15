package com.ch.cloud.nacos.service;

import com.ch.mybatis.service.IService;
import com.ch.cloud.nacos.domain.NacosCluster;

/**
 * 业务-nacos集群Service接口
 *
 * @author admin
 * @since 2022-04-27 13:43:58
 */
public interface INacosClusterService extends IService<NacosCluster> {
    NacosCluster findByUrl(String url);
}