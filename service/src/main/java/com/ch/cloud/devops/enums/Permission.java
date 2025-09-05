package com.ch.cloud.devops.enums;

import lombok.Getter;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/27
 */
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
    
    public static Permission fromCode(String code) {
        for (Permission permission : Permission.values()) {
            if (permission.code.equals(code)) {
                return permission;
            }
        }
        return null;
    }
}
