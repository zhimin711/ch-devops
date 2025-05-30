package com.ch.cloud.nacos.manager;

import com.ch.cloud.devops.dto.UserProjectNamespaceDto;

import java.util.List;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/30
 */
public interface NacosProjectManager {
    
    
    boolean saveUserNamespacePermissions(List<UserProjectNamespaceDto> userProjectNamespaceList);
}
