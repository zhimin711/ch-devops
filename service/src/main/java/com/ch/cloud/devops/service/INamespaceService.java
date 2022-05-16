package com.ch.cloud.devops.service;

import com.ch.mybatis.service.IService;
import com.ch.cloud.devops.dto.Namespace;

import java.io.Serializable;
import java.util.List;

/**
 * 业务-命名空间Service接口
 *
 * @author admin
 * @since 2022-04-27 14:33:17
 */
public interface INamespaceService extends IService<Namespace> {

    Namespace findByUid(String uid);

    List<Namespace> findByClusterIdAndName(Long clusterId, String name);

    Namespace findAuth(Integer namespaceId, String userId);

    Namespace findWithCluster(Serializable namespaceId);

}
