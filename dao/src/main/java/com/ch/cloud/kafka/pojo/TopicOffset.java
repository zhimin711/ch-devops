package com.ch.cloud.kafka.pojo;

import lombok.Data;

/**
 * @author zhimin
 * @date 2022/3/27 7:07 下午
 */
@Data
public class TopicOffset {
    private String topic;
    private int partition;
    private Long consumerOffset;
    private Long beginningOffset;
    private Long endOffset;
    private String groupId;
}
