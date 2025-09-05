package com.ch.cloud.nacos.manager.impl;

import com.ch.cloud.devops.dto.UserProjectNamespaceDto;
import com.ch.cloud.devops.enums.Permission;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.nacos.manager.NacosProjectManager;
import com.ch.cloud.nacos.service.INacosNamespaceProjectService;
import com.ch.cloud.types.NamespaceType;
import com.ch.cloud.upms.client.UpmsUserClient;
import com.ch.e.Assert;
import com.ch.e.PubError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/30
 */
@Service
public class NacosProjectManagerImpl implements NacosProjectManager {
    
    @Autowired
    private UpmsUserClient upmsUserClient;
    
    @Autowired
    private INacosNamespaceProjectService nacosNamespaceProjectService;
    
    @Autowired
    private IUserNamespaceService userNamespaceService;
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveUserNamespacePermissions(List<UserProjectNamespaceDto> userProjectNamespaceList) {
        UserProjectNamespaceDto dto = userProjectNamespaceList.get(0);
        List<UserProjectNamespaceDto> list = userNamespaceService.listUserNamespacesByType(NamespaceType.NACOS,
                dto.getUserId(), dto.getProjectId(), null);
        Map<Long, UserProjectNamespaceDto> namespaceMap = list.stream()
                .collect(Collectors.toMap(UserProjectNamespaceDto::getNamespaceId, e -> e));
        userProjectNamespaceList.forEach(userProjectNamespace -> {
            Assert.isTrue(namespaceMap.containsKey(userProjectNamespace.getNamespaceId()), PubError.NOT_EXISTS,
                    "用户权限" + userProjectNamespace.getNamespaceId());
            Permission permission = Permission.fromCode(userProjectNamespace.getPermission());
            if (permission == null) {
                userNamespaceService.remove(userProjectNamespace.getUserId(), dto.getProjectId(),
                        userProjectNamespace.getNamespaceId());
            } else {
                userNamespaceService.updatePermission(userProjectNamespace.getUserId(), dto.getProjectId(),
                        userProjectNamespace.getNamespaceId(), permission);
            }
        });
        return true;
    }
}
