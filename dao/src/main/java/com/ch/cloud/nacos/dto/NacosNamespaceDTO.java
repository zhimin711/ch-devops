package com.ch.cloud.nacos.dto;

import lombok.Data;

/**
 * <p>
 * desc:Nacos 命名空间详情
 * </p>
 *
 * @author zhimin.ma
 * @since 2021/9/28
 */
@Data
public class NacosNamespaceDTO {

    /**
     * 当前配置数
     */
    private Integer configCount;
    /**
     * 命名空间ID
     */
    private String  namespace;
    /**
     * 命名空间名称
     */
    private String  namespaceShowName;
    /**
     * 配额
     */
    private Integer quota;
    /**
     * the enum of namespace.
     * 0 : Global configuration， 1 : Default private namespace ，2 : Custom namespace.
     */
    private Integer type;
    /**
     * 描述
     */
    private String  namespaceDesc;
}
