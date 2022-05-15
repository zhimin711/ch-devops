package com.ch.cloud.nacos.client;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.dto.ConfigDTO;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.ConfigQueryVO;
import com.ch.cloud.nacos.vo.ConfigVO;
import com.ch.cloud.nacos.vo.ConfigsQueryVO;
import com.ch.result.InvokerPage;
import com.ch.utils.BeanUtilsV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/4/25 23:31
 */
@Component
@Slf4j
public class NacosConfigsClient {

    @Autowired
    private RestTemplate restTemplate;

    public int add(ClientEntity<ConfigVO> entity) {
        return save(entity, true);
    }

    public int edit(ClientEntity<ConfigVO> entity) {
        return save(entity, false);
    }

    private int save(ClientEntity<ConfigVO> entity, boolean isNew) {
        Map<String, String> param = BeanUtilsV2.objectToMap(entity.getData());
//        Boolean sync;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(param, headers);
        Boolean ok = restTemplate.postForObject(entity.getUrl() + NacosAPI.CONFIGS, httpEntity, Boolean.class);
        if (Boolean.TRUE.equals(ok)) return 1;
        return 0;
    }

    public InvokerPage.Page<ConfigDTO> fetchPage(ClientEntity<ConfigsQueryVO> entity) {
        Map<String, String> param = BeanUtilsV2.objectToMap(entity.getData());
        String urlParams = HttpUtil.toParams(param);
        JSONObject resp = restTemplate.getForObject(entity.getUrl() + NacosAPI.CONFIGS + "?" + urlParams, JSONObject.class);
        if (resp != null && resp.containsKey("totalCount")) {
            Integer count = resp.getInteger("totalCount");
            if (count <= 0) {
                return InvokerPage.build();
            }
            JSONArray arr = resp.getJSONArray("pageItems");
            List<ConfigDTO> records = arr.toJavaList(ConfigDTO.class);
            return InvokerPage.build(count, records);
        }
        return InvokerPage.build();
    }

    public void delete(ClientEntity<String> entity) {

    }

    public ConfigDTO fetch(ClientEntity<ConfigQueryVO> entity) {
        Map<String, String> param = BeanUtilsV2.objectToMap(entity.getData());
        String urlParams = HttpUtil.toParams(param);
        JSONObject resp = restTemplate.getForObject(entity.getUrl() + NacosAPI.CONFIGS + "?" + urlParams, JSONObject.class);
        if (resp != null) return resp.toJavaObject(ConfigDTO.class);
        return null;
    }
}
