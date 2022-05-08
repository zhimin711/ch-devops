package com.ch.cloud.rocketmq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 描述：Topic Collected Data
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicCollectDTO {

    private String topic;

    private Date timestamp;

    private BigDecimal inTps;

    private Long inMsgCntToday;

    private BigDecimal outTps;

    private Long outMsgCntToday;

}
