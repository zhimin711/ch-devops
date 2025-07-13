package com.ch.cloud.devops.manager.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.devops.domain.NamespaceApplyRecord;
import com.ch.cloud.devops.dto.NamespaceApplyDto;
import com.ch.cloud.devops.enums.Permission;
import com.ch.cloud.devops.manager.INamespaceApplyManager;
import com.ch.cloud.devops.mapper2.UserProjectNamespaceMapper;
import com.ch.cloud.devops.service.INamespaceApplyRecordService;
import com.ch.core.data.status.ApproveStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/30
 */
@Service
public class NamespaceApplyManagerImpl implements INamespaceApplyManager {
    
    @Resource
    private INamespaceApplyRecordService namespaceApplyRecordService;
    
    @Resource
    private UserProjectNamespaceMapper userProjectNamespaceMapper;
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int approveNacos(NamespaceApplyRecord record) {
        if (ApproveStatus.fromValue(record.getStatus()) != ApproveStatus.SUCCESS) {
            return namespaceApplyRecordService.update(record);
        }
        JSONObject object = JSONObject.parseObject(record.getContent());
        String userId = object.getString("userId");
        Long projectId = object.getLong("projectId");
        JSONArray array = object.getJSONArray("namespaceList");
        List<NamespaceApplyDto> list = array.toJavaList(NamespaceApplyDto.class);
        list.forEach(e -> {
            if (userProjectNamespaceMapper.exists(projectId, userId, e.getNamespaceId()) > 0) {
                String permission = userProjectNamespaceMapper.selectPermission(projectId, userId, e.getNamespaceId());
                Permission p = Permission.fromCode(permission);
                Permission u = e.getPermission();
                if (p != null) {
                    if ((p == Permission.R && u == Permission.W) || (p == Permission.W && u == Permission.R)) {
                        u = Permission.RW;
                    }
                }
                userProjectNamespaceMapper.updatePermission(projectId, userId, e.getNamespaceId(), u.getCode());
            } else {
                userProjectNamespaceMapper.insert(projectId, userId, e.getNamespaceId(), e.getPermission().getCode());
            }
        });
        return namespaceApplyRecordService.update(record);
    }
}
