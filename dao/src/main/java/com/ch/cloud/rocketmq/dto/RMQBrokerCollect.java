package com.ch.cloud.rocketmq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
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
@Table(name = "rmq_broker_collect")
public class RMQBrokerCollect {
    
    @Id
    private Long id;
    
    private String nameSrvAddr;
    
    private String broker;
    
    private Date collectDate;
    
    private Date collectTime;

    private BigDecimal averageTps;

}
