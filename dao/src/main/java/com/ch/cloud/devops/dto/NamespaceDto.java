package com.ch.cloud.devops.dto;

import com.ch.cloud.types.NamespaceType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "命名空间详情")
public class NamespaceDto {
    
    @Schema(description = "主键ID")
    private Long id;
    
    @Schema(description = "集群ID")
    private Long clusterId;
    
    @Schema(description = "命名空间唯一标识")
    private String uid;
    
    @Schema(description = "空间名称")
    private String name;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "同步到Nacos状态：0.未同步 1.已同步")
    private NamespaceType type;
    
    @Schema(description = "创建时间")
    private Date createAt;
    
    @Schema(description = "创建人")
    private String createBy;
    
    @Schema(description = "更新时间")
    private Date updateAt;
    
    @Schema(description = "更新人")
    private String updateBy;
    
    @Schema(description = "当前配置数")
    private Integer configCount;
    
    @Schema(description = "配额")
    private Integer quota;
}
