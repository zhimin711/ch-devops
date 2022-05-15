package com.ch.cloud.nacos.client;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.dto.HistoryDTO;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.HistoryPageVO;
import com.ch.cloud.nacos.vo.HistoryQueryVO;
import com.ch.result.InvokerPage;
import com.ch.utils.BeanUtilsV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class NacosHistoryClient {

    @Autowired
    private RestTemplate restTemplate;

    public InvokerPage.Page<HistoryDTO> fetchPage(ClientEntity<HistoryPageVO> entity) {
        Map<String, String> param = BeanUtilsV2.objectToMap(entity.getData());
        String urlParams = HttpUtil.toParams(param);
        String url = entity.getUrl() + NacosAPI.HISTORY + "?" + urlParams;
        log.info("nacos history page url: {}", url);
        JSONObject resp = restTemplate.getForObject(url, JSONObject.class);
        if (resp != null && resp.containsKey("totalCount")) {
            Integer count = resp.getInteger("totalCount");
            if (count <= 0) {
                return InvokerPage.build();
            }
            JSONArray arr = resp.getJSONArray("pageItems");
            List<HistoryDTO> records = arr.toJavaList(HistoryDTO.class);
            return InvokerPage.build(count, records);
        }
        return InvokerPage.build();
    }

    public HistoryDTO fetch(ClientEntity<HistoryQueryVO> clientEntity) {
        Map<String, String> param = BeanUtilsV2.objectToMap(clientEntity.getData());
        String urlParams = HttpUtil.toParams(param);
        JSONObject resp = restTemplate.getForObject(clientEntity.getUrl() + NacosAPI.HISTORY + "?" + urlParams, JSONObject.class);
        if (resp != null) return resp.toJavaObject(HistoryDTO.class);
        return null;
    }
}
