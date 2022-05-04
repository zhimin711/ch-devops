package com.ch.cloud.nacos.domain;

import com.ch.cloud.types.NamespaceType;
import com.ch.mybatis.context.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 业务-命名空间对象 bt_namespace
 *
 * @author admin
 * @date 2022-04-27 14:33:17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("业务-命名空间")
@Table(name = "bt_namespace")
public class Namespace extends BaseEntity {

    // private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    @ApiModelProperty(name = "集群ID")
    private Long clusterId;

    /**
     * 命名空间唯一标识
     */
    @ApiModelProperty(name = "命名空间唯一标识")
    private String uid;

    /**
     * 命名空间类型：NACOS rocketMQ Kafka
     */
    @ApiModelProperty(name = "命名空间类型")
    @Column
    private NamespaceType type;

    /**
     * 空间名称
     */
    @ApiModelProperty(name = "空间名称")
    private String name;

    /**
     * 描述
     */
    @ApiModelProperty(name = "描述")
    private String description;

    @Transient
    private String addr;
}