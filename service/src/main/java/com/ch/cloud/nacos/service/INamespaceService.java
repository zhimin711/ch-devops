package com.ch.cloud.nacos.service;

import com.ch.mybatis.service.IService;
import com.ch.cloud.nacos.domain.Namespace;

import java.util.List;

/**
 * 业务-命名空间Service接口
 *
 * @author admin
 * @date 2022-04-27 14:33:17
 */
public interface INamespaceService extends IService<Namespace> {

    Namespace findByUid(String uid);

    List<Namespace> findByClusterIdAndName(Long clusterId, String name);
}
