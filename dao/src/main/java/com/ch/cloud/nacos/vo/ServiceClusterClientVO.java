package com.ch.cloud.nacos.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceClusterClientVO extends NamespaceClientVO {

    private String serviceName;

    private String clusterName;

    private Integer checkPort;

    private Boolean useInstancePort4Check;

    private String metadata;

    private String healthChecker;
}
