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
public class ConfigQueryClientVO extends NamespaceClientVO {

    private String show;

    private String tenant;

    private String dataId;

    private String group;

}
