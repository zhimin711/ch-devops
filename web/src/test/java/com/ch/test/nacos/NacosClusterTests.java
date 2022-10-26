package com.ch.test.nacos;

import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
public class NacosClusterTests {

    String url = "http://192.168.0.201:8848/nacos/v1";

    @Test
    public void page() {
        JSONObject resp = new RestTemplate().getForObject(url + NacosAPI.CLUSTER_NODES, JSONObject.class);
        System.out.println(resp.toJSONString());
    }
}
