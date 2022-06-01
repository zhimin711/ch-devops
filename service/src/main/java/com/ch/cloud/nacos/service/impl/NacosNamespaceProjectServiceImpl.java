package com.ch.cloud.nacos.service.impl;

import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.dto.ProjectNamespaceDTO;
import com.ch.cloud.devops.mapper2.NamespaceProjectsMapper;
import com.ch.cloud.devops.vo.ProjectNamespaceVO;
import com.ch.cloud.nacos.service.INacosNamespaceProjectService;
import com.ch.cloud.types.NamespaceType;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/14 10:42
 */
@Service
public class NacosNamespaceProjectServiceImpl implements INacosNamespaceProjectService {

    @Autowired
    private NamespaceProjectsMapper namespaceProjectsMapper;

    @Override
    public List<Long> findProjectIdsByNamespaceId(Long namespaceId) {
        return namespaceProjectsMapper.findProjectIdByNamespaceId(namespaceId);
    }

    @Override
    public Integer assignNamespaceProjects(Long namespaceId, List<Long> projectIds) {
        List<Long> uList = namespaceProjectsMapper.findProjectIdByNamespaceId(namespaceId);

        AtomicInteger c = new AtomicInteger();
        if (!projectIds.isEmpty()) {//1，2，3 | 3、4、5
            projectIds.stream().filter(r -> !uList.contains(r)).forEach(r -> c.getAndAdd(namespaceProjectsMapper.insert(namespaceId, r)));
            uList.stream().filter(r -> !projectIds.contains(r)).forEach(r -> c.getAndAdd(namespaceProjectsMapper.delete(namespaceId, r)));
        } else if (!uList.isEmpty()) {
            uList.forEach(r -> c.getAndAdd(namespaceProjectsMapper.delete(namespaceId, r)));
        }
        return c.get();
    }

    @Override
    public List<Long> findNamespaceIdsByProjectId(Long projectId) {
        return namespaceProjectsMapper.findNamespaceIdByProjectId(projectId);
    }

    @Override
    public Integer assignProjectNamespaces(Long projectId, Long clusterId, List<ProjectNamespaceVO> namespaceVOS) {
        List<Long> namespaceIds = CommonUtils.isNotEmpty(namespaceVOS) ? namespaceVOS.stream().map(ProjectNamespaceVO::getNamespaceId).collect(Collectors.toList()) : Lists.newArrayList();
        List<Long> uList = namespaceProjectsMapper.findNamespaceIdsByProjectIdAndClusterIdAndNamespaceType(projectId, clusterId, NamespaceType.NACOS.name());

        AtomicInteger c = new AtomicInteger();
        if (!namespaceIds.isEmpty()) {//1，2，3 | 3、4、5
            namespaceVOS.forEach(r -> {
                if (uList.contains(r.getNamespaceId())) {
                    c.getAndAdd(namespaceProjectsMapper.update(r.getNamespaceId(), projectId, r.getGroupId()));
                } else {
                    c.getAndAdd(namespaceProjectsMapper.insert2(r.getNamespaceId(), projectId, r.getGroupId()));
                }
            });
            uList.stream().filter(r -> !namespaceIds.contains(r)).forEach(r -> c.getAndAdd(namespaceProjectsMapper.delete(r, projectId)));
        } else if (!uList.isEmpty()) {
            uList.forEach(r -> c.getAndAdd(namespaceProjectsMapper.delete(r, projectId)));
        }
        return c.get();
    }

    @Override
    public List<Long> findClusterIdsByProjectIdAndNamespaceType(Long projectId, NamespaceType namespaceType) {
        return namespaceProjectsMapper.findClusterIdsByProjectIdAndNamespaceType(projectId, namespaceType.name());
    }

    @Override
    public List<NamespaceDto> findNamespacesByProjectIdAndClusterId(Long projectId, Long clusterId) {
        return namespaceProjectsMapper.findNamespacesByProjectIdAndClusterIdAndNamespaceType(projectId, clusterId, NamespaceType.NACOS.name());
    }

    @Override
    public List<ProjectNamespaceDTO> findByProjectIdAndClusterId(Long projectId, Long clusterId) {
        return namespaceProjectsMapper.findByProjectIdAndClusterIdAndNamespaceType(projectId, clusterId, NamespaceType.NACOS.name());
    }

    @Override
    public ProjectNamespaceDTO findByProjectIdAndNamespaceId(Long projectId, Long namespaceId) {
        return namespaceProjectsMapper.findByProjectIdAndNamespaceId(projectId, namespaceId);
    }
}
