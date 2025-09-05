package com.ch.cloud.rocketmq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
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
@Table(name = "rmq_topic_collect")
public class RMQTopicCollect {
    
    @Id
    private Long id;
    
    private String nameSrvAddr;

    private String topic;

    private Date collectDate;
    
    private Date collectTime;

    private BigDecimal inTps;

    private Long inMsgCntToday;

    private BigDecimal outTps;

    private Long outMsgCntToday;

}
