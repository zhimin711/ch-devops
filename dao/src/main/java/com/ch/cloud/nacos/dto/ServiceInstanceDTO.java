package com.ch.cloud.nacos.dto;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/8 16:58
 */
@Data
public class ServiceInstanceDTO {

    private String serviceName;
    private String groupName;

    private Map<String, JSONObject> clusterMap;

    private Map<String, String> metadata;
}
