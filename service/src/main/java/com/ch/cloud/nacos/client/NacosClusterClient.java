package com.ch.cloud.nacos.client;

import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.ServiceClusterVO;
import com.ch.utils.CommonUtils;
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
public class NacosClusterClient extends BaseClient {


    public Object fetchNodes(String url) {
        JSONObject resp = restTemplate.getForObject(url + NacosAPI.CLUSTER_NODES, JSONObject.class);
        if (resp != null && resp.containsKey("data")) {
            return resp.getJSONArray("data");
        }
        return null;
    }

    public Boolean save(ClientEntity<ServiceClusterVO> clientEntity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formParameters(clientEntity), headers);
        String resp = "";
        ResponseEntity<String> resp2 = restTemplate.exchange(clientEntity.getUrl() + NacosAPI.CLUSTER_OP, HttpMethod.PUT, httpEntity, String.class);
        if (resp2.getStatusCode() == HttpStatus.OK) resp = resp2.getBody();
        return CommonUtils.isEquals("ok", resp);
    }
}
