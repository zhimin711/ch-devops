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
    public boolean exists(String userId, String namespaceId, Long projectId) {
        return userProjectNamespaceMapper.countByUserIdAndNamespaceIdAndProjectId(userId, namespaceId, projectId) > 0;
    }
    
    @Override
    public List<UserProjectNamespaceDto> listUserNamespacesByType(String username,
            Long projectId, Long clusterId, NamespaceType namespaceType) {
        return userProjectNamespaceMapper.listUserNamespacesByType(username, projectId, clusterId, namespaceType.name());
    }
    
    @Override
    public List<NamespaceDto> findNamespacesByUsernameAndProjectIdAndNamespaceType(String username, Long projectId,
            NamespaceType namespaceType) {
        return userProjectNamespaceMapper.findNamespacesByUsernameAndProjectIdAndNamespaceType(username, projectId,
                namespaceType.name());
    }
    
    @Override
    public boolean existsPermission(String username, String namespaceId, Long projectId, Permission permission) {
        return userProjectNamespaceMapper.countByUserIdAndNamespaceIdAndProjectIdLikePermission(username, namespaceId,
                projectId, permission.getCode()) > 0;
    }
}
