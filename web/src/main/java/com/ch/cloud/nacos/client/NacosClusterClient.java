package com.ch.cloud.nacos.client;

import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * desc:
 *
 * @author zhimin
 * @date 2022/4/25 23:31
 */
@Component
public class NacosClusterClient {

    @Autowired
    private RestTemplate restTemplate;

    public Object fetchNodes(String url) {
        JSONObject resp = restTemplate.getForObject(url + NacosAPI.CLUSTER_NODES, JSONObject.class);
        if (resp != null && resp.containsKey("data")) {
            return resp.getJSONArray("data");
        }
        return null;
    }
}
