package com.ch.cloud.nacos.client;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.dto.ServiceDTO;
import com.ch.cloud.nacos.dto.ServiceDetailDTO;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.ServiceVO;
import com.ch.cloud.nacos.vo.ServicesPageVO;
import com.ch.cloud.nacos.vo.ServicesQueryVO;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.result.InvokerPage;
import com.ch.utils.BeanUtilsV2;
import com.ch.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
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
public class NacosServicesClient {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * fetch nacos services page
     *
     * @param clientEntity query params
     * @return Page
     */
    public InvokerPage.Page<ServiceDTO> fetchPage(ClientEntity<ServicesPageVO> clientEntity) {
        Map<String, Object> param = BeanUtilsV2.getDeclaredFieldValueMap(clientEntity.getData());
        String urlParams = HttpUtil.toParams(param);
        String url = clientEntity.getUrl() + NacosAPI.SERVICES + "?" + urlParams;
        log.info("nacos services page url: {}", url);
        JSONObject resp = restTemplate.getForObject(url, JSONObject.class);
        if (resp != null && resp.containsKey("count")) {
            Integer count = resp.getInteger("count");
            if (count <= 0) {
                return InvokerPage.build();
            }
            JSONArray arr = resp.getJSONArray("serviceList");
            List<ServiceDTO> records = arr.toJavaList(ServiceDTO.class);
            return InvokerPage.build(count, records);
        }
        return InvokerPage.build();
    }

    public ServiceDetailDTO fetch(ClientEntity<ServicesQueryVO> clientEntity) {
        Map<String, Object> param = BeanUtilsV2.getDeclaredFieldValueMap(clientEntity.getData());
        String urlParams = HttpUtil.toParams(param);
        String url = clientEntity.getUrl() + NacosAPI.SERVICE + "?" + urlParams;
        log.info("nacos service detail url: {}", url);
        return restTemplate.getForObject(url, ServiceDetailDTO.class);
    }

    public Boolean save(ClientEntity<ServiceVO> clientEntity, boolean isNew) {
        Map<String, String> param = BeanUtilsV2.objectToMap(clientEntity.getData());
        MultiValueMap<String, Object> formParameters = new LinkedMultiValueMap<>();
        param.forEach(formParameters::add);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formParameters, headers);
        String resp = "";
        if (isNew) {
            try {
                resp = restTemplate.postForObject(clientEntity.getUrl() + NacosAPI.SERVICE_OP, httpEntity, String.class);
            } catch (Exception e) {
                if (e.getMessage().contains("already exists")) {
                    ExceptionUtils._throw(PubError.EXISTS, clientEntity.getData().getServiceName() + "@" + clientEntity.getData().getGroupName());
                }
                throw e;
            }
        } else {
            ResponseEntity<String> resp2 = restTemplate.exchange(clientEntity.getUrl() + NacosAPI.SERVICE_OP, HttpMethod.PUT, httpEntity, String.class);
            if (resp2.getStatusCode() == HttpStatus.OK) resp = resp2.getBody();
        }
        return CommonUtils.isEquals("ok", resp);
    }

    public Boolean delete(ClientEntity<ServicesQueryVO> clientEntity) {
        Map<String, Object> param = BeanUtilsV2.getDeclaredFieldValueMap(clientEntity.getData());
        String urlParams = HttpUtil.toParams(param);
        String url = clientEntity.getUrl() + NacosAPI.SERVICE_OP + "?" + urlParams;

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
        if (resp.getStatusCode() == HttpStatus.OK) {
            log.info("delete service: {}", resp.getBody());
            return CommonUtils.isEquals(resp.getBody(), "ok");
        }
        return false;
    }
}
