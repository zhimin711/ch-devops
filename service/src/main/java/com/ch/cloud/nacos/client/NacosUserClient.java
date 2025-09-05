package com.ch.cloud.nacos.client;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.conf.NacosProperties;
import com.ch.cloud.nacos.dto.NacosTokenDTO;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.NamespaceClientVO;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.ch.utils.NumberUtils;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Date;
import java.util.Map;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/4/25 23:31
 */
@Component
public class NacosUserClient extends BaseClient {
    
    @Autowired
    private NacosProperties nacosProperties;
    
    private final static Map<String, String> TOKEN_MAP = Maps.newConcurrentMap();
    
    public void login(ClientEntity<? extends NamespaceClientVO> clientEntity) {
        if (!CommonUtils.isNotEmpty(clientEntity.getUsername(), clientEntity.getPassword())) {
            return;
        }
        if (clientEntity.getData() == null) {
            return;
        }
        if (TOKEN_MAP.containsKey(clientEntity.getUrl())) {
            String token = TOKEN_MAP.get(clientEntity.getUrl());
            if (validateToken(token)) {
                clientEntity.getData().setAccessToken(token);
                return;
            }
            TOKEN_MAP.remove(clientEntity.getUrl());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> formParameters = new LinkedMultiValueMap<>();
        formParameters.add("username", clientEntity.getUsername());
        formParameters.add("password", clientEntity.getPassword());
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formParameters, headers);
        NacosTokenDTO tokenDTO = invoke(
                clientEntity.getUrl() + (CommonUtils.isEmpty(nacosProperties.getLoginApi()) ? NacosAPI.OPEN_API_LOGIN
                        : nacosProperties.getLoginApi()), HttpMethod.POST, httpEntity, NacosTokenDTO.class);
        Assert.notNull(tokenDTO, PubError.USERNAME_OR_PASSWORD);
        clientEntity.getData().setAccessToken(tokenDTO.getAccessToken());
        TOKEN_MAP.put(clientEntity.getUrl(), tokenDTO.getAccessToken());
    }
    
    private boolean validateToken(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        Object obj = jwt.getPayload("exp");
        if (obj != null && NumberUtils.isNumeric(obj.toString())) {
            Long timestamp = Long.parseLong(obj.toString()) * 1000;
            Date date = DateUtils.parseTimestamp(timestamp);
            return date.after(DateUtils.current());
        }
        return false;
    }
}
