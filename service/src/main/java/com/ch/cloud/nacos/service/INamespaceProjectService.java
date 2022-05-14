package com.ch.cloud.nacos.service;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @date 2022/5/14 10:41
 */
public interface INamespaceProjectService {

    List<Long> findProjectIdsByNamespaceId(Long namespaceId);

    Integer assignNamespaceProjects(Long namespaceId, List<Long> projectIds);

    List<Long> findNamespaceIdsByProjectId(Long projectId);

    Integer assignProjectNamespaces(Long projectId, List<Long> namespaceIds);
}
