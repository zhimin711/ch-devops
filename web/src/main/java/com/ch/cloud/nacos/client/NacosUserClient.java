package com.ch.cloud.nacos.client;

import com.alibaba.fastjson.JSON;
import com.ch.cloud.nacos.vo.NamespaceVO;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.utils.AssertUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.ServiceClusterVO;
import com.ch.utils.CommonUtils;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/4/25 23:31
 */
@Component
public class NacosUserClient extends BaseClient {

    public int login(ClientEntity<? extends NamespaceVO> clientEntity) {
        if (!CommonUtils.isNotEmpty(clientEntity.getUsername(), clientEntity.getPassword())) {
            return -1;
        }
        String resp = restTemplate.getForObject(clientEntity.getUrl() + NacosAPI.LOGIN, String.class);
        JSONObject data = null;
        try {
            data = JSON.parseObject(resp);
        } catch (Exception e) {
            ExceptionUtils._throw(PubError.USERNAME_OR_PASSWORD, resp);
        }
        String token = data.getString("accessToken");
        clientEntity.getData().setAccessToken(token);
        return data.getInteger("tokenTtl");
    }
}
