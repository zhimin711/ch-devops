package com.ch.cloud.nacos.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 描述： export config
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ConfigExportVO extends NamespaceVO {

    private Boolean export;

    private Boolean exportV2;

    private String dataId;
    private String group;
    private String appName;

    private String tenant;

    private String ids;
}
