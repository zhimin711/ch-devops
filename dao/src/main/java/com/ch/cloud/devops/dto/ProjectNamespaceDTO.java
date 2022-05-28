package com.ch.cloud.devops.dto;

import lombok.Data;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/28 14:04
 */
@Data
public class ProjectNamespaceDTO {

    private Long   projectId;
    private Long   namespaceId;
    private String groupId;
}
