package com.ch.cloud.nacos.vo;

import lombok.Data;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Data
public class PageSubcribersVO {

    private Integer clusterId;

    private Integer namespaceId;

    private String serviceName;

    private String groupName;
}
