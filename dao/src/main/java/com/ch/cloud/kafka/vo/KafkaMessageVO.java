package com.ch.cloud.kafka.vo;

import lombok.Data;

/**
 * ContentSearchDto 扩展对象
 *
 * @author zhimin.ma
 * @since Wed Oct 30 17:38:37 CST 2019
 */
@Data
public class KafkaMessageVO {

    /**
     * 集群
     */
    private Long clusterId;

    /**
     * 主题
     */
    private String topic;
    private int    partition;
    private String key;
    private String value;

}