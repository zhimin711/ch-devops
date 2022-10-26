package com.ch.cloud.nacos.client;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.dto.ServiceDTO;
import com.ch.cloud.nacos.dto.ServiceDetailDTO;
import com.ch.cloud.nacos.dto.ServiceInstanceDTO;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.ServiceVO;
import com.ch.cloud.nacos.vo.ServicesPageVO;
import com.ch.cloud.nacos.vo.ServicesQueryVO;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.result.InvokerPage;
import com.ch.utils.BeanUtilsV2;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

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
public class NacosServicesClient extends BaseClient {

    /**
     * fetch nacos services page
     *
     * @param clientEntity
     *            query params
     * @return Page
     */
    public InvokerPage.Page<ServiceDTO> fetchPage(ClientEntity<ServicesPageVO> clientEntity) {
        String url = urlWithAll(NacosAPI.SERVICES, clientEntity);
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
        String url = urlWithAll(NacosAPI.SERVICE, clientEntity);
        log.info("Nacos service detail url: {}", url);
        return invoke(url, HttpMethod.GET, HttpEntity.EMPTY, ServiceDetailDTO.class);
    }

    public Boolean save(ClientEntity<ServiceVO> clientEntity, boolean isNew) {

        String url = url(NacosAPI.SERVICE_OP, clientEntity);
        log.info("Nacos service save url: {}", url);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = formHttpEntity(clientEntity);
        String resp = "";
        if (isNew) {
            try {
                resp = restTemplate.postForObject(url, httpEntity, String.class);
            } catch (Exception e) {
                if (e.getMessage().contains("already exists")) {
                    ExceptionUtils._throw(PubError.EXISTS,
                        clientEntity.getData().getServiceName() + "@" + clientEntity.getData().getGroupName());
                }
                throw e;
            }
        } else {
            ResponseEntity<String> resp2 = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
            if (resp2.getStatusCode() == HttpStatus.OK)
                resp = resp2.getBody();
        }
        return CommonUtils.isEquals("ok", resp);
    }

    public Boolean delete(ClientEntity<ServicesQueryVO> clientEntity) {
        String url = urlWithAll(NacosAPI.SERVICE_OP, clientEntity);
        log.info("Nacos service delete url: {}", url);
        String resp = invoke(url, HttpMethod.DELETE, null, String.class);
        return CommonUtils.isEquals(resp, "ok");
    }

    public List<ServiceInstanceDTO> fetchList(ClientEntity<ServicesPageVO> clientEntity) {
        String url = urlWithAll(NacosAPI.SERVICES, clientEntity);
        log.info("nacos services list with instances url: {}", url);
        JSONArray array = invoke(url, HttpMethod.GET, null, JSONArray.class);
        if (array.isEmpty())
            return Lists.newArrayList();
        return array.toJavaList(ServiceInstanceDTO.class);
    }
}
