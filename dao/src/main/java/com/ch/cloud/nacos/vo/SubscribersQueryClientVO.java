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
public class SubscribersQueryClientVO extends NamespaceClientVO {

    private int pageNo = 1;

    private int pageSize = 10;

    private String serviceName;

    private String groupName;
}
