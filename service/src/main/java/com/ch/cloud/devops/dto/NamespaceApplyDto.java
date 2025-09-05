package com.ch.cloud.devops.dto;

import com.ch.cloud.devops.enums.Permission;
import lombok.Data;
import lombok.Getter;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/4/4
 */
@Data
public class NamespaceApplyDto {
    
    private Long namespaceId;
    
    /**
     * 权限： read or write
     */
    private Permission permission;
    
}
