package com.ch.cloud.nacos.controller;

import com.ch.cloud.nacos.client.NacosServicesClient;
import com.ch.cloud.nacos.dto.ServiceDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.ServicesPageVO;
import com.ch.cloud.nacos.vo.ServicesQueryVO;
import com.ch.result.PageResult;
import com.ch.result.ResultUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@RestController
@RequestMapping("/nacos/services")
public class NacosServicesController {

    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    @Autowired
    private NacosServicesClient     nacosServicesClient;


    @ApiOperation(value = "查询分页", notes = "分页查询服务")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<ServiceDTO> page(ServicesPageVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<ServicesPageVO> clientEntity = nacosNamespaceValidator.validConfig(record);
            return nacosServicesClient.fetchPage(clientEntity);
        });
    }

    @ApiOperation(value = "查询详情", notes = "查询服务详情")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<ServiceDTO> detail(ServicesQueryVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<ServicesQueryVO> clientEntity = nacosNamespaceValidator.validConfig(record);
//            return nacosServicesClient.fetchPage(clientEntity);
            return null;
        });
    }

}
