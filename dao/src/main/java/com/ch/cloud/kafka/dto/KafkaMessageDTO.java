package com.ch.cloud.kafka.dto;

import lombok.Data;

/**
 * @author zhimin.ma
 * @since 2021/4/10 0:56
 */
@Data
public class KafkaMessageDTO {
    private String topic;
    private int    partition;
    private Long   offset;
    private Long   timestamp;
    private String key;
    private String value;
}
