package com.ch.cloud.nacos.dto;

import lombok.Data;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/8 16:58
 */
@Data
public class ServiceDTO {

    private String  clusterCount;
    private String  groupName;
    private String  healthyInstanceCount;
    private String  ipCount;
    private String  name;
    private String  triggerFlag;
}
