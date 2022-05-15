package com.ch.cloud.nacos;

/**
 * desc:base url [/nacos/v1]
 *
 * @author zhimin
 * @since 2022/4/23 16:04
 */
public interface NacosAPI {

    String CLUSTER_NODES = "/core/cluster/nodes";
    String NAMESPACES = "/console/namespaces";
    String CONFIGS = "/cs/configs";
    String SERVICES = "/ns/catalog/services";
    String SUBSCRIBERS = "/ns/service/subscribers";
}
