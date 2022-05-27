package com.ch.cloud.nacos.service;

import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.types.NamespaceType;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/14 10:41
 */
public interface INacosNamespaceProjectService {

    List<Long> findProjectIdsByNamespaceId(Long namespaceId);

    Integer assignNamespaceProjects(Long namespaceId, List<Long> projectIds);

    List<Long> findNamespaceIdsByProjectId(Long projectId);

    Integer assignProjectNamespaces(Long projectId, List<Long> namespaceIds);

    List<Long> findClusterIdsByProjectIdAndNamespaceType(Long projectId, NamespaceType namespaceType);

    List<NamespaceDto> findNamespacesByProjectIdAndClusterId(Long projectId, Long clusterId);
}
