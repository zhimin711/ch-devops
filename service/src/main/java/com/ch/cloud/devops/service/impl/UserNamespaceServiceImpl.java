package com.ch.cloud.devops.service.impl;

import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.mapper2.UserProjectNamespaceMapper;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.types.NamespaceType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/5/16
 */
@Service
public class UserNamespaceServiceImpl implements IUserNamespaceService {

    @Resource
    private UserProjectNamespaceMapper userProjectNamespaceMapper;

    @Override
    public List<NamespaceDto> findNamespacesByUsernameAndProjectId(String username, Long projectId, NamespaceType type) {
        return userProjectNamespaceMapper.findNamespacesByUserIdAndProjectId(username, projectId, type.name());
    }

    @Override
    public boolean exists(String userId, String namespaceId, Long projectId) {
        return userProjectNamespaceMapper.countByUserIdAndNamespaceIdAndProjectId(userId, namespaceId, projectId) > 0;
    }
}
