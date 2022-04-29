package com.ch.cloud.nacos.vo;

import lombok.Data;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Data
public class SubscribersQueryVO {

    private String namespaceId;

    private int pageNo = 1;

    private int pageSize = 10;

    private String serviceName;

    private String groupName;
}
