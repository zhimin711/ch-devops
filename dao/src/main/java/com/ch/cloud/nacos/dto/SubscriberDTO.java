package com.ch.cloud.nacos.dto;

import lombok.Data;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/8 16:58
 */
@Data
public class SubscriberDTO {

    private String  addrStr;
    private String  agent;
    private String  app;
    private String  cluster;
    private String  ip;
    private String  namespaceId;
    private String  port;
    private String  serviceName;
}
