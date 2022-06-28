package com.ch.cloud.nacos.client;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.NamespaceVO;
import com.ch.e.PubError;
import com.ch.utils.AssertUtils;
import com.ch.utils.BeanUtilsV2;
import com.ch.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/18 21:28
 */
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
        param.forEach(formParameters::add);

        return formParameters;
    }

    protected <T> String urlParams(ClientEntity<T> clientEntity) {
        Map<String, Object> param = BeanUtilsV2.getDeclaredFieldValueMap(clientEntity.getData());
        return HttpUtil.toParams(param);
    }

}
