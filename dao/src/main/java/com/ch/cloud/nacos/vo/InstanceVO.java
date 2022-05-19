package com.ch.cloud.nacos.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/8 16:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstanceVO extends NamespaceVO {

    private String clusterName;
    private String serviceName;
    private String groupName;

    private Boolean enabled;
    private Boolean ephemeral;

    private String instanceId;
    private String ip;
    private Integer port;
    private Integer weight;

    private String metadata;
}
