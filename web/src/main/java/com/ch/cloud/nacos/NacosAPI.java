package com.ch.cloud.nacos;

/**
 * desc:base url [/nacos/v1]
 *
 * @author zhimin
 * @since 2022/4/23 16:04
 */
public interface NacosAPI {

    String LOGIN = "/auth/users/login";
    String CLUSTER_NODES = "/core/cluster/nodes";
    String CLUSTER_OP = "/ns/cluster";
    String NAMESPACES = "/console/namespaces";
    String CONFIGS = "/cs/configs";
    String SERVICES = "/ns/catalog/services";
    String SERVICE = "/ns/catalog/service";
    String SERVICE_OP = "/ns/service";
    String INSTANCES = "/ns/catalog/instances";
    String INSTANCE_OP = "/ns/instance";
    String SUBSCRIBERS = "/ns/service/subscribers";

    String HISTORY = "/cs/history";
}
