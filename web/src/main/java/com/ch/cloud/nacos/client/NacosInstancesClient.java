package com.ch.cloud.nacos.client;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.dto.InstanceDTO;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.InstancesPageVO;
import com.ch.result.InvokerPage;
import com.ch.utils.BeanUtilsV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * desc: nacos 服务 client
 *
 * @author zhimin
 * @since 2022/4/25 23:31
 */
@Component
@Slf4j
public class NacosInstancesClient {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * fetch nacos instances page
     *
     * @param clientEntity query params
     * @return Page
     */
    public InvokerPage.Page<InstanceDTO> fetchPage(ClientEntity<InstancesPageVO> clientEntity) {
        Map<String, Object> param = BeanUtilsV2.getDeclaredFieldValueMap(clientEntity.getData());
//        log.info("namespace: {}, client data: {}", clientEntity.getData().getNamespaceId(), param);
        String urlParams = HttpUtil.toParams(param);
        String url = clientEntity.getUrl() + NacosAPI.INSTANCES + "?" + urlParams;
        log.info("nacos instances page url: {}", url);
        JSONObject resp = restTemplate.getForObject(url, JSONObject.class);
        if (resp != null && resp.containsKey("count")) {
            Integer count = resp.getInteger("count");
            if (count <= 0) {
                return InvokerPage.build();
            }
            if (resp.containsKey("list")) {
                JSONArray arr = resp.getJSONArray("list");
                List<InstanceDTO> records = arr.toJavaList(InstanceDTO.class);
                return InvokerPage.build(count, records);
            } else {
                log.info("instances result: {}",resp.toJSONString());
            }
        }
        return InvokerPage.build();
    }
}
