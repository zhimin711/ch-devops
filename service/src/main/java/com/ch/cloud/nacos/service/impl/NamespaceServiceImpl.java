package com.ch.cloud.nacos.service.impl;

import com.ch.mybatis.service.ServiceImpl;;
import com.ch.utils.CommonUtils;
import org.springframework.stereotype.Service;
import com.ch.cloud.nacos.mapper.NamespaceMapper;
import com.ch.cloud.nacos.domain.Namespace;
import com.ch.cloud.nacos.service.INamespaceService;

/**
 * 业务-命名空间Service业务层处理
 *
 * @author admin
 * @date 2022-04-27 14:33:17
 */
@Service
public class NamespaceServiceImpl extends ServiceImpl<NamespaceMapper, Namespace> implements INamespaceService {

    @Override
    public Namespace findByUid(String uid) {
        if (CommonUtils.isEmpty(uid)) return null;
        Namespace record = new Namespace();
        record.setUid(uid);
        return getMapper().selectOne(record);
    }
}