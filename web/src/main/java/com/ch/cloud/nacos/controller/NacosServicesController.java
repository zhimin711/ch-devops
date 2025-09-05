package com.ch.cloud.nacos.controller;

import com.ch.cloud.nacos.client.NacosClusterClient;
import com.ch.cloud.nacos.client.NacosServicesClient;
import com.ch.cloud.nacos.dto.ServiceDTO;
import com.ch.cloud.nacos.dto.ServiceDetailDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.*;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Tag(name = "Nacos实例服务")
@RestController
@RequestMapping("/nacos/services")
public class NacosServicesController {

    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    @Autowired
    private NacosServicesClient nacosServicesClient;
    @Autowired
    private NacosClusterClient nacosClusterClient;

    @Operation(summary = "查询分页", description = "分页查询服务")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<ServiceDTO> page(ServicesPageClientVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<ServicesPageClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosServicesClient.fetchPage(clientEntity);
        });
    }

    @Operation(summary = "查询详情", description = "查询服务详情")
    @GetMapping
    public Result<ServiceDetailDTO> detail(ServicesQueryClientVO record) {
        return ResultUtils.wrap(() -> {
            ClientEntity<ServicesQueryClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosServicesClient.fetch(clientEntity);
        });
    }

    @Operation(summary = "添加", description = "添加服务")
    @PostMapping
    public Result<Boolean> add(@RequestBody ServiceClientVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ServiceClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosServicesClient.save(clientEntity, true);
        });
    }

    @Operation(summary = "修改", description = "修改服务")
    @PutMapping
    public Result<Boolean> edit(@RequestBody ServiceClientVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ServiceClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosServicesClient.save(clientEntity, false);
        });
    }

    @Operation(summary = "删除", description = "删除服务")
    @DeleteMapping
    public Result<Boolean> delete(ServicesQueryClientVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ServicesQueryClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosServicesClient.delete(clientEntity);
        });
    }

    @Operation(summary = "修改", description = "修改服务")
    @PutMapping("cluster")
    public Result<Boolean> editCluster(@RequestBody ServiceClusterClientVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ServiceClusterClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosClusterClient.save(clientEntity);
        });
    }
}