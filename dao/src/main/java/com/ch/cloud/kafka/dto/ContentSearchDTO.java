package com.ch.cloud.kafka.dto;

import lombok.Data;

/**
 * ContentSearchDto 扩展对象
 * 
 * @author zhimin.ma
 * @since Wed Oct 30 17:38:37 CST 2019
 */
@Data
public class ContentSearchDTO {

    /**
     * 集群
     */
    private String cluster;

    /**
     * 主题
     */
    private String topic;

    /**
     * 描述
     */
    private String content;

}