package com.ch.cloud.devops.service;

import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.types.NamespaceType;

import java.util.List;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/5/16
 */
public interface IUserNamespaceService {

    List<NamespaceDto> findNamespacesByUsernameAndProjectId(String username, Long projectId, NamespaceType type);

    boolean exists(String userId, String namespaceId, Long projectId);
}
