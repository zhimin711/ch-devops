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
public class ConfigsPageVO extends NamespaceVO {

    private String search;

    private String tenant;

    private String dataId;

    private String group;

    private String appName;

    private String config_tags;

    private int pageNo = 1;

    private int pageSize = 10;
}
