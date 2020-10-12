package com.ch.cloud.kafka.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 内容搜索条件
 *
 * @author zhimin.ma
 * @date 2018/9/19 17:00
 */
@ApiModel("Kafka消息搜索参数")
@Data
public class ContentQuery {

    /**
     * 集群名称
     */
    @ApiModelProperty(value = "集群名称", required = true, position = 1)
    private String cluster;
    /**
     * 集群主题
     */
    @ApiModelProperty(value = "集群主题", required = true, position = 2)
    private String topic;
    /**
     * 搜索类型(0.全量 1.按最新 2.最早 3.)
     */
    @ApiModelProperty(value = "搜索类型(0.全量 1.按最新 2.最早)", required = true, position = 3)
    private String type;
    /**
     * 内容
     */
    @ApiModelProperty(value = "搜索内容", position = 4)
    private String content;

    @ApiModelProperty(value = "搜索页", hidden = true)
    private int page = 1;

    @ApiModelProperty(value = "搜索量", position = 6)
    private int limit = 1000;

}
