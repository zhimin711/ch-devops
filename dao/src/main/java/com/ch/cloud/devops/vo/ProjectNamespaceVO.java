package com.ch.cloud.devops.vo;

import lombok.Data;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/28 14:04
 */
@Data
public class ProjectNamespaceVO {

    private Long   projectId;
    
    private Long   namespaceId;
    
    private String groupId;
}
