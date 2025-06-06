package com.ch.cloud.rocketmq.mapper;


import com.ch.cloud.rocketmq.dto.RMQTopicCollect;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.List;

public interface RMQTopicCollectMapper  extends Mapper<RMQTopicCollect>, InsertListMapper<RMQTopicCollect> {
    
    @Select("SELECT max(id) FROM rmq_topic_collect WHERE name_srv_addr = #{nameSrvAddr} AND collect_date = #{current} GROUP BY topic")
    List<Long> groupTopicLastCollectId(String nameSrvAddr, Date current);
}
