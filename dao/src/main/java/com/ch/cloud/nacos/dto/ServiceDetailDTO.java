package com.ch.cloud.nacos.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/8 16:58
 */
@Data
public class ServiceDetailDTO {

    private JSONObject service;

    private List<JSONObject> clusters;

}
