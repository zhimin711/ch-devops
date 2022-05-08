package com.ch.cloud.nacos.vo;

import lombok.Data;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Data
public class ConfigsQueryVO {

    private String namespaceId;

    private String search;

    private String tenant;

    private String dataId;

    private String group;

    private String appName;

    private String config_tags;

    private int pageNo = 1;

    private int pageSize = 10;
}
