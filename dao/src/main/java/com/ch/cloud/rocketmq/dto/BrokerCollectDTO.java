package com.ch.cloud.rocketmq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 描述：Broker Collected Data
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerCollectDTO {

    private Long clusterId;

    private String broker;

    private Date timestamp;

    private BigDecimal totalTps;

}
