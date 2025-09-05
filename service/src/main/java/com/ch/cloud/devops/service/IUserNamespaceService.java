package com.ch.cloud.devops.service;

import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.dto.UserProjectNamespaceDto;
import com.ch.cloud.devops.enums.Permission;
import com.ch.cloud.types.NamespaceType;

import java.util.List;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/5/16
 */
public interface IUserNamespaceService {

    boolean exists(String userId, Long namespaceId, Long projectId);

    List<UserProjectNamespaceDto> listUserNamespacesByType(NamespaceType namespaceType, String username, Long projectId, List<Long> clusterIds);
    
    List<NamespaceDto> listByUsernameAndProjectIdAndNamespaceType(String userId, Long projectId, NamespaceType namespaceType);
    
    boolean existsPermission(String username, Long namespaceId, Long projectId, Permission permission);
    
    boolean remove(String userId, Long projectId, Long namespaceId);
    
    boolean updatePermission(String userId, Long projectId, Long namespaceId, Permission permission);
}
