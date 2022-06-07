package com.ch.cloud.kafka.vo;

import com.ch.cloud.kafka.enums.SearchType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 内容搜索条件
 *
 * @author zhimin.ma
 * @since 2018/9/19 17:00
 */
@ApiModel("Kafka消息搜索参数")
@Data
public class KafkaContentSearchVO {

    /**
     * 集群名称
     */
    @ApiModelProperty(name = "集群ID", required = true, position = 1)
    private Long clusterId;
    /**
     * 集群主题
     */
    @ApiModelProperty(name = "集群主题", required = true, position = 2)
    private String topic;
    /**
     * 搜索类型(0.全量 1.按最新 2.最早 3.)
     */
    @ApiModelProperty(name = "搜索类型", value = "(0.全量 1.按最新 2.最早)", required = true, position = 3)
    private SearchType type;

    @ApiModelProperty(name = "搜索量", position = 6, example = "1000")
    private int limit = 1000;
    /**
     * 内容
     */
    @ApiModelProperty(name = "搜索Key", position = 5)
    private String key;
    /**
     * 内容
     */
    @ApiModelProperty(name = "搜索内容", position = 4)
    private String content;

    @ApiModelProperty(name = "搜索分区")
    private Integer partition;

    @ApiModelProperty(name = "搜索offset")
    private Integer offset = 0;


}
