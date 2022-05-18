package com.ch.cloud.nacos.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.dto.InstanceDTO;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.InstanceVO;
import com.ch.cloud.nacos.vo.InstancesPageVO;
import com.ch.result.InvokerPage;
import com.ch.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * desc: nacos 服务 client
 *
 * @author zhimin
 * @since 2022/4/25 23:31
 */
@Component
@Slf4j
public class NacosInstancesClient extends BaseClient {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * fetch nacos instances page
     *
     * @param clientEntity query params
     * @return Page
     */
    public InvokerPage.Page<InstanceDTO> fetchPage(ClientEntity<InstancesPageVO> clientEntity) {
        String urlParams = urlParams(clientEntity);
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
                log.info("instances result: {}", resp.toJSONString());
            }
        }
        return InvokerPage.build();
    }

    public Boolean save(ClientEntity<InstanceVO> clientEntity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formParams(clientEntity), headers);
        String resp = "";
        ResponseEntity<String> resp2 = restTemplate.exchange(clientEntity.getUrl() + NacosAPI.INSTANCE_OP, HttpMethod.PUT, httpEntity, String.class);
        if (resp2.getStatusCode() == HttpStatus.OK) resp = resp2.getBody();
        return CommonUtils.isEquals("ok", resp);
    }
}
