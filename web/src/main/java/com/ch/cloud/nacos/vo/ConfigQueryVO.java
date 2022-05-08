package com.ch.cloud.nacos.vo;

import lombok.Data;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Data
public class ConfigQueryVO {

    private String namespaceId;

    private String show;

    private String tenant;

    private String dataId;

    private String group;

}
