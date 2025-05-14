package com.ch.cloud.devops.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.devops.mapper2.UserProjectNamespaceMapper;
import com.ch.mybatis.service.ServiceImpl;
import com.ch.s.ApproveStatus;
import org.springframework.stereotype.Service;
import com.ch.cloud.devops.mapper.NamespaceApplyRecordMapper;
import com.ch.cloud.devops.domain.NamespaceApplyRecord;
import com.ch.cloud.devops.service.INamespaceApplyRecordService;

import javax.annotation.Resource;

/**
 * 申请空间记录Service业务层处理
 *
 * @author admin
 * @since 2022-05-21 11:52:27
 */
@Service
public class NamespaceApplyRecordServiceImpl extends ServiceImpl<NamespaceApplyRecordMapper, NamespaceApplyRecord> implements INamespaceApplyRecordService {

    @Resource
    private UserProjectNamespaceMapper userProjectNamespaceMapper;

    @Override
    public int approveNacos(NamespaceApplyRecord record) {
        if (ApproveStatus.fromValue(record.getStatus()) != ApproveStatus.SUCCESS) {
            return super.update(record);
        }
        JSONObject object = JSONObject.parseObject(record.getContent());
        String userId = object.getString("userId");
        Long projectId = object.getLong("projectId");
        JSONArray array = object.getJSONArray("namespaceIds");
        array.stream().filter(e -> userProjectNamespaceMapper.exists(projectId, userId, e.toString()) <= 0)
                .forEach(e -> userProjectNamespaceMapper.insert(projectId, userId, e.toString()));
        return super.update(record);
    }
}