package com.ch.cloud.rocketmq.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 描述：Topic Collected Data
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Data
public class TopicCollectDTO {

    private String topic;

    private Long timestamp;

    private BigDecimal inTps;

    private Long inMsgCntToday;

    private BigDecimal outTps;

    private Long outMsgCntToday;
}
