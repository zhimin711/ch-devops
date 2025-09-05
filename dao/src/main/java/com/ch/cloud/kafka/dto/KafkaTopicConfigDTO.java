package com.ch.cloud.kafka.dto;

import lombok.Data;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/6/5 20:36
 */
@Data
public class KafkaTopicConfigDTO {
    private String  name;
    private String  value;
    private boolean _default;
    private boolean readonly;
    private boolean sensitive;
}
