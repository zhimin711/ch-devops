package com.ch.cloud.devops.domain;

import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.types.NamespaceType;
import com.ch.mybatis.context.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 业务-命名空间对象 bt_namespace
 *
 * @author admin
 * @since 2022-04-27 14:33:17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "业务-命名空间", contains = BaseEntity.class)
@Table(name = "bt_namespace")
public class Namespace extends BaseEntity {

    /**
     * 集群ID
     */
    @Schema(description = "clusterId", title = "集群ID")
    private Long clusterId;

    /**
     * 命名空间唯一标识
     */
    @Schema(description = "命名空间唯一标识")
    private String uid;

    /**
     * 命名空间类型：NACOS rocketMQ Kafka
     */
    @Schema(description = "命名空间类型")
    @Column
    private NamespaceType type;

    /**
     * 空间名称
     */
    @Schema(description = "空间名称")
    private String name;

    /**
     * 描述
     */
    @Schema(description = "空间描述")
    private String description;

    /**
     * 管理角色（数据权限-修改空间下数据）
     */
    @Schema(description = "管理角色")
    private String roles;

    @Transient
    private NacosCluster cluster;

    @Transient
    private Integer configCount;
    
    @Transient
    private Integer quota;
}