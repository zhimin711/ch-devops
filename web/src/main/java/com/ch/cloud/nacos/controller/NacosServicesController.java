package com.ch.cloud.nacos.controller;

import com.ch.cloud.nacos.client.NacosServicesClient;
import com.ch.cloud.nacos.dto.ServiceDTO;
import com.ch.cloud.nacos.dto.ServiceDetailDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.*;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            ClientEntity<ServicesPageVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosServicesClient.fetchPage(clientEntity);
        });
    }

    @ApiOperation(value = "查询详情", notes = "查询服务详情")
    @GetMapping
    public Result<ServiceDetailDTO> detail(ServicesQueryVO record) {
        return ResultUtils.wrap(() -> {
            ClientEntity<ServicesQueryVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosServicesClient.fetch(clientEntity);
        });
    }

    @ApiOperation(value = "添加", notes = "添加配置")
    @PostMapping
    public Result<Boolean> add(@RequestBody ServiceVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ServiceVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosServicesClient.save(clientEntity, true);
        });
    }

    @ApiOperation(value = "修改", notes = "修改配置")
    @PutMapping
    public Result<Boolean> edit(@RequestBody ServiceVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ServiceVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosServicesClient.save(clientEntity, false);
        });
    }

    @ApiOperation(value = "删除", notes = "删除配置")
    @DeleteMapping
    public Result<Boolean> delete(ServicesQueryVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ServicesQueryVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosServicesClient.delete(clientEntity);
        });
    }

}
