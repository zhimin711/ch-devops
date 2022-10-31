package com.ch.cloud.nacos.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/15 11:14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NacosNamespaceClientVO extends NamespaceClientVO {

    private String name;

    private String desc;

}
