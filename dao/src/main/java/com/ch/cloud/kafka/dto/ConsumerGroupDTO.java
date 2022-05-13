package com.ch.cloud.kafka.dto;

import lombok.Data;

import java.util.Set;

/**
 * @author zhimin
 * @date 2022/4/2 20:35 下午
 */
@Data
public class ConsumerGroupDTO {

    private String      groupId;
    private Long        lag;
    private Set<String> topics;
}
