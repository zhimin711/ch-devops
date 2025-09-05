package com.ch.cloud.rocketmq.mapper;


import com.ch.cloud.rocketmq.dto.RMQBrokerCollect;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface RMQBrokerCollectMapper extends Mapper<RMQBrokerCollect>, InsertListMapper<RMQBrokerCollect> {

}
