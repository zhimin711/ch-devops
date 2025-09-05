package com.ch.cloud.devops.domain;

import com.ch.mybatis.context.BaseEntityWithStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Table;
import java.util.Date;

/**
 * 申请空间记录对象 bt_apply_record
 *
 * @author admin
 * @since 2022-05-21 11:52:27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "申请空间记录")
@Table(name = "bt_namespace_apply_record")
public class NamespaceApplyRecord extends BaseEntityWithStatus {

    // private static final long serialVersionUID = 1L;

    /**
     * 类型：1:nacos-namespace 2: 3:
     */
    @Schema(description = "类型：1")
    private Integer type;

    /**
     * 申请KEY:可以是数据ID
     */
    @Schema(description = "申请KEY")
    private String dataKey;

    /**
     * 申请内容
     */
    @Schema(description = "申请内容")
    private String content;

    /**
     * 审核人
     */
    @Schema(description = "审核人")
    private String approveBy;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private Date approveAt;

}