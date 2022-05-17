package com.ch.cloud.nacos.dto;

import com.alibaba.fastjson.JSON;
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

    private JSON service;

    private List<?> clusters;

}
