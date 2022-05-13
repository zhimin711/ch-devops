package com.ch.cloud.kafka.pojo;

import lombok.Data;

/**
 * @author zhimin
 * @date 2022/4/5 11:29 上午
 */
@Data
public class ResetOffset {
    private int partition;
    private String seek;
    private Long offset;
}
