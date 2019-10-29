package com.ch.cloud.kafka.pojo;

import lombok.Data;

/**
 * 内容搜索条件
 *
 * @author 01370603
 * @date 2018/9/19 17:00
 */
@Data
public class ContentQuery {

    /**
     * 集群名称
     */
    private String cluster;
    /**
     * 集群主题
     */
    private String topic;
    /**
     * 搜索类型(0.全量 1.按最新 2.最早 3.)
     */
    private String type;
    /**
     * 内容
     */
    private String content;

    private int page = 1;

    private int limit = 1000;

}
