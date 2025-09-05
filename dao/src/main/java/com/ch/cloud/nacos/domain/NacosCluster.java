package com.ch.cloud.nacos.domain;

import com.ch.mybatis.context.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Table;

/**
 * 业务-nacos集群对象 bt_nacos_cluster
 *
 * @author admin
 * @since 2022-04-27 13:43:58
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "业务-nacos集群")
@Table(name = "bt_nacos_cluster")
public class NacosCluster extends BaseEntity {

    // private static final long serialVersionUID = 1L;

    /**
     * 集群API
     */
    @Schema(description = "集群API")
    private String url;

    /**
     * 空间名称
     */
    @Schema(description = "集群名称")
    private String name;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;
    /**
     * 序号（排序）
     */
    @Schema(description = "序号（排序）")
    private Integer sort;

}