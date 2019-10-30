package com.ch.cloud.kafka.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ContentSearchDto 扩展对象
 * 
 * @author 01370603
 * @date Wed Oct 30 17:38:37 CST 2019
 */
@Data
public class ContentSearchDto {

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