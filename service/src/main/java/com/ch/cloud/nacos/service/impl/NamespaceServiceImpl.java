package com.ch.cloud.nacos.service.impl;

import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.domain.Namespace;
import com.ch.cloud.nacos.mapper.NamespaceMapper;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.nacos.service.INamespaceService;
import com.ch.mybatis.service.ServiceImpl;
import com.ch.mybatis.utils.ExampleUtils;
import com.ch.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.Sqls;

import java.io.Serializable;
import java.util.List;

/**
 * 业务-命名空间Service业务层处理
 *
 * @author admin
 * @date 2022-04-27 14:33:17
 */
@Service
public class NamespaceServiceImpl extends ServiceImpl<NamespaceMapper, Namespace> implements INamespaceService {

    @Autowired
    private INacosClusterService nacosClusterService;


    @Override
    public Namespace findByUid(String uid) {
        if (CommonUtils.isEmpty(uid))
            return null;
        Namespace record = new Namespace();
        record.setUid(uid);
        return getMapper().selectOne(record);
    }

    @Override
    public List<Namespace> findByClusterIdAndName(Long clusterId, String name) {
        if (CommonUtils.isEmpty(clusterId))
            return null;
        Namespace record = new Namespace();
        record.setClusterId(clusterId);
        Sqls sqls = Sqls.custom();
        ExampleUtils.dynCommon(sqls, record);
        ExampleUtils.dynEqual(sqls, record, "clusterId");

        Example ex = Example.builder(Namespace.class)
                .where(sqls)
//                .orderByDesc("createAt", "id")
                .build();
        return getMapper().selectByExample(ex);
    }

    @Override
    public Namespace findAuth(Integer namespaceId, String userId) {
        return null;
    }

    @Override
    public Namespace findWithCluster(Serializable namespaceId) {
        Namespace n = super.find(namespaceId);
        if (n != null) {
            NacosCluster s = nacosClusterService.find(n.getClusterId());
            if (s != null) n.setAddr(s.getUrl());
        }
        return n;
    }

}