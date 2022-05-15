package com.ch.cloud.nacos.domain;

import com.ch.mybatis.context.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Table;

/**
 * 业务-nacos集群对象 bt_nacos_cluster
 *
 * @author admin
 * @since 2022-04-27 13:43:58
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("业务-nacos集群")
@Table(name = "bt_nacos_cluster")
public class NacosCluster extends BaseEntity {

    // private static final long serialVersionUID = 1L;

    /**
     * 集群API
     */
    @ApiModelProperty(name = "集群API")
    private String url;

    /**
     * 空间名称
     */
    @ApiModelProperty(name = "集群名称")
    private String name;

    /**
     * 描述
     */
    @ApiModelProperty(name = "描述")
    private String description;

}