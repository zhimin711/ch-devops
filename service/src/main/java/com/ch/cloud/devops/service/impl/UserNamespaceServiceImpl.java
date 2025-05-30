package com.ch.cloud.devops.service.impl;

import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.dto.UserProjectNamespaceDto;
import com.ch.cloud.devops.enums.Permission;
import com.ch.cloud.devops.mapper2.UserProjectNamespaceMapper;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.types.NamespaceType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/5/16
 */
@Service
public class UserNamespaceServiceImpl implements IUserNamespaceService {
    
    @Resource
    private UserProjectNamespaceMapper userProjectNamespaceMapper;
    
    @Override
    public boolean exists(String userId, Long namespaceId, Long projectId) {
        return userProjectNamespaceMapper.countByUserIdAndNamespaceIdAndProjectId(userId, namespaceId, projectId) > 0;
    }
    
    @Override
    public List<UserProjectNamespaceDto> listUserNamespacesByType(NamespaceType namespaceType, String username,
            Long projectId, List<Long> clusterIds) {
        return userProjectNamespaceMapper.listUserNamespacesByType(namespaceType.name(), username, projectId,
                clusterIds);
    }
    
    @Override
    public List<NamespaceDto> listByUsernameAndProjectIdAndNamespaceType(String username, Long projectId,
            NamespaceType namespaceType) {
        return userProjectNamespaceMapper.findNamespacesByUsernameAndProjectIdAndNamespaceType(username, projectId,
                namespaceType.name());
    }
    
    @Override
    public boolean existsPermission(String username, Long namespaceId, Long projectId, Permission permission) {
        return userProjectNamespaceMapper.countByUserIdAndNamespaceIdAndProjectIdLikePermission(username, namespaceId,
                projectId, permission.getCode()) > 0;
    }
    
    @Override
    public boolean remove(String userId, Long projectId, Long namespaceId) {
        return userProjectNamespaceMapper.delete(projectId, userId, namespaceId) > 0;
    }
    
    @Override
    public boolean updatePermission(String userId, Long projectId, Long namespaceId, Permission permission) {
        return userProjectNamespaceMapper.updatePermission(projectId, userId, namespaceId, permission.getCode()) > 0;
    }
}
