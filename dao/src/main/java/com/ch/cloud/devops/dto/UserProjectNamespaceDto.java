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

    private String userId;
    private Long   projectId;
    private Long   namespaceId;
}
