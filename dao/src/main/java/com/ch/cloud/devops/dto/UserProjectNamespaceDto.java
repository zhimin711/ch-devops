package com.ch.cloud.devops.dto;

import lombok.Data;

/**
 * <p>
 * desc:
 * </p>
 *
 * @author zhimin.ma
 * @since 2021/10/25
 */
@Data
public class UserProjectNamespaceDto {
    
    private Long namespaceId;
    
    private String namespaceName;
    
    private String nacosNamespaceId;
    
    private String permission;
    
    private String userId;
    
    private Long clusterId;
    
    private String clusterName;
    
    private Long projectId;
}
