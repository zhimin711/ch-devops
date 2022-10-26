package com.ch.cloud.nacos.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubscribesPageVO  extends NamespaceVO {

    private int pageNo = 1;

    private int pageSize = 10;

    private String serviceName;

    private String groupName;
}