package com.ch.cloud.rocketmq.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 描述：Broker Collected Data
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Data
public class BrokerCollectDTO {

    private String broker;

    private Long timestamp;

    private BigDecimal totalTps;

}
