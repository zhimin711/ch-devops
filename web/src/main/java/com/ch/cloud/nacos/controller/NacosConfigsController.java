package com.ch.cloud.nacos.controller;

import com.ch.cloud.nacos.client.NacosConfigsClient;
import com.ch.cloud.nacos.dto.ConfigDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.*;
import com.ch.cloud.upms.client.UpmsProjectClientService;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：配置
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@RestController
@RequestMapping("/nacos/configs")
public class NacosConfigsController {

    @Autowired
    private NacosConfigsClient nacosConfigsClient;
    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    @Autowired
    private UpmsProjectClientService upmsProjectClientService;

    @ApiOperation(value = "分页查询", notes = "分页查询nacos配置")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<ConfigDTO> page(ConfigsPageVO record) {

        return ResultUtils.wrapPage(() -> {
            ClientEntity<ConfigsPageVO> entity = nacosNamespaceValidator.valid(record);
            record.setTenant(record.getNamespaceId());
            record.setNamespaceId(null);
            return nacosConfigsClient.fetchPage(entity);
        });
    }

    @ApiOperation(value = "查询", notes = "查询配置详情")
    @GetMapping
    public Result<ConfigDTO> get(ConfigQueryVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigQueryVO> clientEntity = nacosNamespaceValidator.valid(record);
            record.setTenant(record.getNamespaceId());
            return nacosConfigsClient.fetch(clientEntity);
        });
    }

    @ApiOperation(value = "添加", notes = "添加配置")
    @PostMapping
    public Result<Boolean> add(@RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.valid(record);
            if (CommonUtils.isDecimal(record.getAppName())) {
                Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(Long.parseLong(record.getAppName()), null);
                record.setGroup(result.get().getCode());
            }
            return nacosConfigsClient.add(clientEntity);
        });
    }

    @ApiOperation(value = "修改", notes = "修改配置")
    @PutMapping
    public Result<Boolean> edit(@RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosConfigsClient.edit(clientEntity);
        });
    }

    @ApiOperation(value = "克隆", notes = "克隆配置")
    @PostMapping("clone")
    public Result<?> clone(ConfigPolicyVO record,
                           @RequestBody ConfigCloneVO[] records) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigPolicyVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosConfigsClient.clone(clientEntity, records);
        });
    }

    @ApiOperation(value = "删除", notes = "删除配置")
    @DeleteMapping
    public Result<Boolean> delete(ConfigDeleteVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigDeleteVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosConfigsClient.delete(clientEntity);
        });
    }

}
