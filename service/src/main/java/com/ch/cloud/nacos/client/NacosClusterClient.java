package com.ch.cloud.nacos.client;

import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.NamespaceClientVO;
import com.ch.cloud.nacos.vo.ServiceClusterClientVO;
import com.ch.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/4/25 23:31
 */
@Component
@Slf4j
public class NacosClusterClient extends BaseClient {

    public Object fetchNodes(ClientEntity<NamespaceClientVO> clientEntity) {
        String url = url(NacosAPI.CLUSTER_NODES, clientEntity);
        log.info("nacos cluster fetchNodes url: {}", url);
        JSONObject resp = restTemplate.getForObject(url, JSONObject.class);
        if (resp != null && resp.containsKey("data")) {
            return resp.getJSONArray("data");
        }
        return null;
    }

    public Boolean save(ClientEntity<ServiceClusterClientVO> clientEntity) {
        String url = url(NacosAPI.CLUSTER_OP, clientEntity);
        log.info("nacos cluster save url: {}", url);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = formHttpEntity(clientEntity);
        String resp = invoke(url, HttpMethod.PUT, httpEntity, String.class);
        return CommonUtils.isEquals("ok", resp);
    }
}
