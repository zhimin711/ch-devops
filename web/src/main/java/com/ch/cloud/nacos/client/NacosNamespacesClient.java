package com.ch.cloud.nacos.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.domain.Namespace;
import com.ch.cloud.nacos.dto.NacosNamespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @date 2022/4/25 23:31
 */
@Component
public class NacosNamespacesClient {

    @Autowired
    private RestTemplate restTemplate;

    public Boolean add(Namespace record) {
        return saveNacosNamespace(record, true);
    }

    public Boolean edit(Namespace record) {
        return saveNacosNamespace(record, false);
    }

    private boolean saveNacosNamespace(Namespace record, boolean isNew) {
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("namespaceDesc", record.getDescription());
        if (isNew) {
            param.add("customNamespaceId", record.getUid());
            param.add("namespaceName", record.getName());
        } else {
            param.add("namespace", record.getUid());
            param.add("namespaceShowName", record.getName());
        }
        Boolean sync;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, headers);
        if (isNew) {
            sync = restTemplate.postForObject(record.getAddr() + NacosAPI.NAMESPACES, httpEntity, Boolean.class);
        } else {
//            restTemplate.put(nacosUrl + NAMESPACE_ADDR, param);
            ResponseEntity<Boolean> resp = restTemplate.exchange(record.getAddr() + NacosAPI.NAMESPACES, HttpMethod.PUT, httpEntity, Boolean.class);
            if (resp.getStatusCode() == HttpStatus.OK) {
                sync = resp.getBody();
            } else {
                return false;
            }
        }
        return sync != null && sync;
    }

    public NacosNamespace fetch(Namespace namespace) {
        String param = "show=all&namespaceId=" + namespace.getUid();
        NacosNamespace nn = restTemplate.getForObject(namespace.getAddr() + NacosAPI.NAMESPACES + "?" + param, NacosNamespace.class);
        return nn;
    }

    public List<NacosNamespace> fetchAll(String url) {
        JSONObject resp = restTemplate.getForObject(url + NacosAPI.NAMESPACES, JSONObject.class);
        if (resp != null && resp.containsKey("data")) {
            JSONArray arr = resp.getJSONArray("data");
            return arr.toJavaList(NacosNamespace.class);
        }
        return null;
    }

    public void delete(Namespace orig) {

    }
}
