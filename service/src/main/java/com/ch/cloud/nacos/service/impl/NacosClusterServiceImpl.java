package com.ch.cloud.nacos.service.impl;

import com.ch.mybatis.service.ServiceImpl;;
import com.ch.utils.CommonUtils;
import org.springframework.stereotype.Service;
import com.ch.cloud.nacos.mapper.NacosClusterMapper;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.service.INacosClusterService;

/**
 * 业务-nacos集群Service业务层处理
 *
 * @author admin
 * @since 2022-04-27 13:43:58
 */
@Service
public class NacosClusterServiceImpl extends  ServiceImpl<NacosClusterMapper, NacosCluster>  implements INacosClusterService {

    @Override
    public NacosCluster findByUrl(String url) {
        if(CommonUtils.isEmpty(url)) return null;
        NacosCluster p = new NacosCluster();
        p.setUrl(url.trim());
        return getMapper().selectOne(p);
    }
}