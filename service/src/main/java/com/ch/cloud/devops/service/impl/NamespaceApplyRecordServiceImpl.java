package com.ch.cloud.devops.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.devops.dto.NamespaceApplyDto;
import com.ch.cloud.devops.mapper2.UserProjectNamespaceMapper;
import com.ch.mybatis.service.ServiceImpl;
import com.ch.s.ApproveStatus;
import org.springframework.stereotype.Service;
import com.ch.cloud.devops.mapper.NamespaceApplyRecordMapper;
import com.ch.cloud.devops.domain.NamespaceApplyRecord;
import com.ch.cloud.devops.service.INamespaceApplyRecordService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 申请空间记录Service业务层处理
 *
 * @author admin
 * @since 2022-05-21 11:52:27
 */
@Service
public class NamespaceApplyRecordServiceImpl extends ServiceImpl<NamespaceApplyRecordMapper, NamespaceApplyRecord>
        implements INamespaceApplyRecordService {
    
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
        JSONArray array = object.getJSONArray("applyList");
        List<NamespaceApplyDto> list = array.toJavaList(NamespaceApplyDto.class);
        list.stream().filter(e -> userProjectNamespaceMapper.exists(projectId, userId, e.getNamespaceId()) <= 0)
                .forEach(e -> userProjectNamespaceMapper.insert(projectId, userId, e.getNamespaceId(),
                        e.getPermission().getCode()));
        return super.update(record);
    }
}