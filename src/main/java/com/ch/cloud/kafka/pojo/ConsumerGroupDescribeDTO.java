package com.ch.cloud.kafka.pojo;

import lombok.Data;

/**
 * @author zhimin
 * @date 2022/4/18 10:10 下午
 */
@Data
public class ConsumerGroupDescribeDTO {
    private String groupId;
    private String topic;
    private int    partition;
    private Long   currentOffset;
    private Long   logBeginningOffset;
    private Long   logEndOffset;
    private Long   lag;
    private String consumerId;
    private String host;
    private String clientId;
}
