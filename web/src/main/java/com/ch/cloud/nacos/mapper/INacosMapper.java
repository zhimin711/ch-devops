package com.ch.cloud.nacos.mapper;

import com.ch.cloud.nacos.vo.ServicesPageVO;
import com.ch.cloud.nacos.vo.SubscribesPageVO;
import com.ch.cloud.nacos.vo.ServicesQueryVO;
import com.ch.cloud.nacos.vo.SubscribersQueryVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Mapper
public interface INacosMapper {

    INacosMapper INSTANCE = Mappers.getMapper(INacosMapper.class);

    ServicesQueryVO toServicesQuery(ServicesPageVO servicesPageVO);

    SubscribersQueryVO toSubscribersQuery(SubscribesPageVO pageServicesVO);
}
