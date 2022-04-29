package com.ch.cloud.nacos;

/**
 * desc:base url [/nacos/v1]
 *
 * @author zhimin
 * @date 2022/4/23 16:04
 */
public interface NacosAPI {

    String CLUSTER_NODES = "/core/cluster/nodes";
    String NAMESPACES = "/nacos/v1/console/namespaces";
    String CONFIGS = "/nacos/v1/cs/configs";
    String SERVICES = "/nacos/v1/ns/catalog/services";
    String SUBSCRIBERS = "/nacos/v1/ns/service/subscribers";
}
