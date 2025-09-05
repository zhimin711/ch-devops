package com.ch.cloud.kafka.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Kafka 主题配置信息")
public class KafkaTopicDTO {


    /**
     * 集群名称
     */
    @Schema(description = "集群ID")
    private Long clusterId;

    /**
     * 主题名称
     */
    @Schema(description = "主题名称")
    private String topicName;

    /**
     * 存储类型：STRING, JSON, PROTO_STUFF
     */
    @Schema(description = "存储类型")
    private String type;

    /**
     * 类文件
     */
    @Schema(description = "类文件")
    private String classFile;

    /**
     * 类名称
     */
    @Schema(description = "类名称")
    private String className;

    /**
     * 分区数
     */
    private Integer partitionSize;

    /**
     * 复制数（备份）
     */
    private Integer replicaSize;


    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 状态：0. 1. 2.
     */
    private String status;

    private String zookeeper;

    private Long totalLogSize;
}