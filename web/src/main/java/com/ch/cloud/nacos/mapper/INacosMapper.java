package com.ch.cloud.nacos.mapper;

import com.ch.cloud.nacos.vo.PageServicesVO;
import com.ch.cloud.nacos.vo.PageSubcribersVO;
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

    ServicesQueryVO toServicesQuery(PageServicesVO pageServicesVO);

    SubscribersQueryVO toSubscribersQuery(PageSubcribersVO pageServicesVO);
}
