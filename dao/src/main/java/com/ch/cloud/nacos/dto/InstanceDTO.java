package com.ch.cloud.nacos.dto;

import lombok.Data;

import java.util.Map;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/8 16:58
 */
@Data
public class InstanceDTO {

    private String clusterName;
    private String serviceName;

    private Boolean enabled;
    private Boolean ephemeral;
    private Boolean healthy;

    private Integer instanceHeartBeatInterval;
    private Integer instanceHeartBeatTimeOut;
    private Integer ipDeleteTimeout;

    private String instanceId;
    private String ip;
    private Integer port;
    private Integer weight;

    private Long lastBeat;

    private Boolean marked;

    private Map<String, String> metadata;
}
