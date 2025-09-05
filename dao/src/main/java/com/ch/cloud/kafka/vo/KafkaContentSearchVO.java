package com.ch.cloud.kafka.vo;

import com.ch.cloud.kafka.enums.SearchType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;

/**
 * 内容搜索条件
 *
 * @author zhimin.ma
 * @since 2018/9/19 17:00
 */
@Schema(description = "Kafka消息搜索参数")
@Data
public class KafkaContentSearchVO {

    /**
     * 集群名称
     */
    @Schema(description = "集群ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long clusterId;
    /**
     * 集群主题
     */
    @Schema(description = "主题ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long topicId;
    
    @Schema(hidden = true)
    private String topic;
    /**
     * 搜索类型(0.全量 1.按最新 2.最早 3.)
     */
    @Schema(description = "搜索类型(0.全量 1.按最新 2.最早)", requiredMode = Schema.RequiredMode.REQUIRED)
    private SearchType type;

    @Schema(description = "搜索结果数量（限制）")
    @Max(500)
    private int limit = 50;

    @Schema(description = "搜索范围大小（限制")
    @Max(4999)
    private Integer size = 1000;
    /**
     * 内容
     */
    @Schema(description = "搜索Key")
    private String key;
    /**
     * 内容
     */
    @Schema(description = "搜索内容")
    private String content;

    @Schema(description = "搜索分区")
    private Integer partition;

    @Schema(description = "最新搜索位置")
    private long offset;

}
