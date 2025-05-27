package com.ch.cloud.devops.dto;

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
    
    private String namespaceId;
    
    /**
     * 权限： read or write
     */
    private Permission permission;
    
    @Getter
    public enum Permission {
        R("r", "只读"),
        W("w", "只写"),
        RW("rw", "读写");
        
        private final String code;
        
        private final String desc;
        
        Permission(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}
