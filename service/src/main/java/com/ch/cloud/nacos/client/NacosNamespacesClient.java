package com.ch.cloud.nacos.client;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.dto.NacosNamespaceDTO;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.NacosNamespaceClientVO;
import com.ch.cloud.nacos.vo.NamespaceClientVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/4/25 23:31
 */
@Component
@Slf4j
public class NacosNamespacesClient extends BaseClient {

    public Boolean add(ClientEntity<NacosNamespaceClientVO> clientEntity) {
        return saveNacosNamespace(clientEntity, true);
    }

    public Boolean edit(ClientEntity<NacosNamespaceClientVO> clientEntity) {
        return saveNacosNamespace(clientEntity, false);
    }

    private boolean saveNacosNamespace(ClientEntity<NacosNamespaceClientVO> clientEntity, boolean isNew) {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("namespaceDesc", clientEntity.getData().getDesc());
        if (isNew) {
            param.add("customNamespaceId", clientEntity.getData().getNamespaceId());
            param.add("namespaceName", clientEntity.getData().getName());
        } else {
            param.add("namespace", clientEntity.getData().getNamespaceId());
            param.add("namespaceShowName", clientEntity.getData().getName());
        }
        Boolean sync;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, headers);
        String url = url(NacosAPI.NAMESPACES, clientEntity);
        log.info("nacos namespace add or edit url: {}", url);
        if (isNew) {
            sync = invoke(url, HttpMethod.POST, httpEntity, Boolean.class);
        } else {
            sync = invoke(url, HttpMethod.PUT, httpEntity, Boolean.class);
        }
        return sync != null && sync;
    }

    public NacosNamespaceDTO fetch(ClientEntity<NamespaceClientVO> clientEntity) {
        String url = url(NacosAPI.NAMESPACES, clientEntity);
        url += "&show=all&namespaceId=" + clientEntity.getData().getNamespaceId();
        log.info("nacos namespace fetch url: {}", url);
        return invoke(url, HttpMethod.GET, HttpEntity.EMPTY, NacosNamespaceDTO.class);
    }

    public List<NacosNamespaceDTO> fetchAll(ClientEntity<NamespaceClientVO> clientEntity) {
        String url = url(NacosAPI.NAMESPACES, clientEntity);
        log.info("nacos namespace fetchAll url: {}", url);
        JSONObject resp = restTemplate.getForObject(url, JSONObject.class);
        if (resp != null && resp.containsKey("data")) {
            JSONArray arr = resp.getJSONArray("data");
            return arr.toJavaList(NacosNamespaceDTO.class);
        }
        return null;
    }

    public Boolean delete(ClientEntity<NamespaceClientVO> clientEntity) {

        String url = urlWithData(NacosAPI.NAMESPACES, clientEntity);
        log.info("nacos namespace delete url: {}", url);
        return invoke(url, HttpMethod.DELETE, HttpEntity.EMPTY, Boolean.class);
    }
}
