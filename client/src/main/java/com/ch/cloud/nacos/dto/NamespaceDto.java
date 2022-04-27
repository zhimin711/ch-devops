package com.ch.cloud.nacos.dto;

import lombok.Data;

import java.util.Date;

/**
 * <p>
 * desc:命名空间详情
 * </p>
 *
 * @author zhimin.ma
 * @since 2021/10/9
 */
@Data
public class NamespaceDto {


    private Long id;

    /**
     * 命名空间唯一标识
     */
    private String uid;

    /**
     * 空间名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 同步到Nacos状态：0.未同步 1.已同步
     */
    private Boolean syncNacos;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private Date updateAt;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 当前配置数
     */
    private Integer configCount;
    /**
     * 配额
     */
    private Integer quota;
}
