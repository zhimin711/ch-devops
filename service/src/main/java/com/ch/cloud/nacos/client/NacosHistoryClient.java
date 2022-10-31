package com.ch.cloud.nacos.client;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.dto.HistoryDTO;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.HistoryPageClientVO;
import com.ch.cloud.nacos.vo.HistoryQueryClientVO;
import com.ch.result.InvokerPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/4/25 23:31
 */
@Component
@Slf4j
public class NacosHistoryClient extends BaseClient {

    public InvokerPage.Page<HistoryDTO> fetchPage(ClientEntity<HistoryPageClientVO> clientEntity) {
        String url = urlWithData(NacosAPI.HISTORY, clientEntity);
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

    public HistoryDTO fetch(ClientEntity<HistoryQueryClientVO> clientEntity) {
        String url = urlWithData(NacosAPI.HISTORY, clientEntity);
        log.info("nacos history fetch url: {}", url);
        return invoke(url, HttpMethod.GET, HttpEntity.EMPTY, HistoryDTO.class);
    }
}
