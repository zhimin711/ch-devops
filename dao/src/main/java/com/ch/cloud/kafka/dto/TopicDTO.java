package com.ch.cloud.kafka.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Kafka 主题配置信息")
public class TopicDTO {


    /**
     * 集群名称
     */
    @ApiModelProperty("集群名称")
    private String clusterName;

    /**
     * 主题名称
     */
    @ApiModelProperty("主题名称")
    private String topicName;

    /**
     * 存储类型：STRING, JSON, PROTO_STUFF
     */
    @ApiModelProperty("存储类型")
    private String type;

    /**
     * 类文件
     */
    @ApiModelProperty("类文件")
    private String classFile;

    /**
     * 类名称
     */
    @ApiModelProperty("类名称")
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
    @ApiModelProperty("描述")
    private String description;

    /**
     * 状态：0. 1. 2.
     */
    private String status;

    private String zookeeper;

}