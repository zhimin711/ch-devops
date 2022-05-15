package com.ch.cloud.nacos.client;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.vo.ServicesQueryVO;
import com.ch.result.InvokerPage;
import com.ch.utils.BeanUtilsV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * desc: nacos 服务 client
 *
 * @author zhimin
 * @since 2022/4/25 23:31
 */
@Component
public class NacosServicesClient {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * fetch nacos services page
     *
     * @param url     cluster url
     * @param queryVO query params
     * @return Page
     */
    public InvokerPage.Page<?> fetchPage(String url, ServicesQueryVO queryVO) {
        String params = HttpUtil.toParams(BeanUtilsV2.objectToMap(queryVO));
        JSONObject resp = restTemplate.getForObject(url + NacosAPI.SERVICES + "?" + params, JSONObject.class);
        if (resp == null) {
            return InvokerPage.build();
        }
        return InvokerPage.build(resp.getInteger("count"), resp.getJSONArray("serviceList"));
    }
}
