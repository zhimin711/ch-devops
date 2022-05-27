package com.ch.cloud.devops.domain;

import java.util.Date;

import com.ch.mybatis.context.BaseEntity;
import com.ch.mybatis.context.BaseEntityWithStatus;
import com.ch.mybatis.context.BaseEntityWithStatusV2;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Table;

/**
 * 申请空间记录对象 bt_apply_record
 *
 * @author admin
 * @since 2022-05-21 11:52:27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("申请空间记录")
@Table(name = "bt_namespace_apply_record")
public class NamespaceApplyRecord extends BaseEntityWithStatus {

    // private static final long serialVersionUID = 1L;

    /**
     * 类型：1:nacos-namespace 2: 3:
     */
    @ApiModelProperty(name = "类型：1")
    private Integer type;

    /**
     * 申请KEY:可以是数据ID
     */
    @ApiModelProperty(name = "申请KEY")
    private String dataKey;

    /**
     * 申请内容
     */
    @ApiModelProperty(name = "申请内容")
    private String content;

    /**
     * 审核人
     */
    @ApiModelProperty(name = "审核人")
    private String approveBy;

    /**
     * 审核时间
     */
    @ApiModelProperty(name = "审核时间")
    private Date approveAt;

}