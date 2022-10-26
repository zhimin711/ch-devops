package com.ch.cloud.nacos.client;

import java.util.Map;

import com.ch.cloud.nacos.vo.NamespaceVO;
import com.ch.utils.CommonUtils;
import com.ch.utils.JSONUtils;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.utils.BeanUtilsV2;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/18 21:28
 */
@Slf4j
public abstract class BaseClient {

    @Autowired
    protected RestTemplate restTemplate;
    @Autowired
    protected RetryTemplate retryTemplate;

    protected <T> MultiValueMap<String, Object> formParameters(ClientEntity<T> clientEntity) {
        MultiValueMap<String, Object> formParameters = new LinkedMultiValueMap<>();
        Map<String, String> param = BeanUtilsV2.objectToMap(clientEntity.getData());
        param.forEach(formParameters::add);
        return formParameters;
    }

    protected <T> MultiValueMap<String, Object> formParams(ClientEntity<T> clientEntity) {
        MultiValueMap<String, Object> formParameters = new LinkedMultiValueMap<>();
        Map<String, Object> param = BeanUtilsV2.getDeclaredFieldValueMap(clientEntity.getData());
        param.remove("accessToken");
        param.forEach(formParameters::add);
        return formParameters;
    }
    protected <T> HttpEntity<MultiValueMap<String, Object>> formHttpEntity(ClientEntity<T> clientEntity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formParams(clientEntity), headers);
//        MultiValueMap<String, Object> formParameters = new LinkedMultiValueMap<>();
//        Map<String, Object> param = BeanUtilsV2.getDeclaredFieldValueMap(clientEntity.getData());
//        param.forEach(formParameters::add);
        return new HttpEntity<>(formParams(clientEntity), headers);
    }

    protected <T extends NamespaceVO> String urlParams(T data) {
        return urlParams(data, false);
    }

    protected <T extends NamespaceVO> String urlParams(T data, boolean containsSupper) {
        Map<String, ?> param;
        if (containsSupper) {
            param = BeanUtilsV2.getDeclaredFieldValueMap(data);
        } else {
            param = BeanUtilsV2.objectToMap(data);
        }
        return HttpUtil.toParams(param);
    }

    protected String url(String api, ClientEntity<? extends NamespaceVO> clientEntity) {
        String url = clientEntity.getUrl() + api;
//        if (CommonUtils.isNotEmpty(clientEntity.getData().getAccessToken())) {
            url += "?accessToken=" + clientEntity.getData().getAccessToken();
//        }
        return url;
    }

    protected String urlWithData(String api, ClientEntity<? extends NamespaceVO> clientEntity) {
        String url = clientEntity.getUrl() + api;
        // if (CommonUtils.isNotEmpty(clientEntity.getData().getAccessToken())) {
        // }
        String param = urlParams(clientEntity.getData());
        url += "?accessToken=" + clientEntity.getData().getAccessToken() + "&" + param;
        return url;
    }

    protected String urlWithAll(String api, ClientEntity<? extends NamespaceVO> clientEntity) {
        String url = clientEntity.getUrl() + api;
        String param = urlParams(clientEntity.getData(),true);
        url += "?" + param;
        return url;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T invoke(String url, HttpMethod httpMethod, HttpEntity<?> httpEntity, Class<T> clazz) {

        return retryTemplate.execute((RetryCallback<T, Throwable>)retryContext -> {
            boolean isBasicClass = BeanUtilsV2.BasicType.isBasic(clazz);
            if (isBasicClass) {
                ResponseEntity<T> resp = restTemplate.exchange(url, httpMethod, httpEntity, clazz);
                if (resp.getStatusCode() == HttpStatus.OK) {
                    return resp.getBody();
                }
                log.error("invokeURL error! {} result: {}", url, resp);
                return null;
            }

            ResponseEntity<String> resp = restTemplate.exchange(url, httpMethod, httpEntity, String.class);
            if (resp.getBody() == null) {
                return null;
            }
            String data = resp.getBody();
            if (!JSONUtils.isArray(data) && !JSONUtils.isObject(data)) {
                log.warn("invokeURL warn! {} result: {}", url, data);
                return null;
            }
            if (clazz.isAssignableFrom(JSONObject.class) && JSONUtils.isObject(data)) {
                return (T)JSONObject.parseObject(data);
            } else if (clazz.isAssignableFrom(JSONArray.class) && JSONUtils.isArray(data)) {
                return (T)JSONArray.parseArray(data);
            }
            try {
                JSONObject obj = JSONObject.parseObject(data);
                return obj.toJavaObject(clazz);
            } catch (Exception e) {
                log.error("解析结果失败：" + resp, e);
                throw e;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T invokeData(String url, HttpEntity<?> httpEntity, Class<T> clazz) throws Throwable {

        return retryTemplate.execute((RetryCallback<T, Throwable>)retryContext -> {
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            if (resp.getStatusCode() != HttpStatus.OK) {
                return null;
            }
            JSONObject obj = JSONObject.parseObject(resp.getBody());
            if (!obj.containsKey("result")) {
                log.warn("invoke not found result: {}", resp);
                return null;
            }
            JSONObject result = obj.getJSONObject("result");
            if (result.containsKey("code") && result.getInteger("code") == 200) {
                if (clazz.isAssignableFrom(JSONObject.class)) {
                    return (T)result.getJSONObject("data");
                }
                if (clazz.isAssignableFrom(JSONArray.class)) {
                    return (T)result.getJSONArray("data");
                }
                return result.getJSONObject("data").toJavaObject(clazz);
            }
            log.info("invoke data: {}", resp);
            return null;
        });
    }

}
