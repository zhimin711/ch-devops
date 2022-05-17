package com.ch.cloud.nacos.controller.user;

import com.ch.cloud.nacos.client.NacosConfigsClient;
import com.ch.cloud.nacos.dto.ConfigDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.*;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
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
@RequestMapping("/nacos/user/{projectId:[0-9]+}/configs")
public class NacosUserConfigsController {

    @Autowired
    private NacosConfigsClient nacosConfigsClient;
    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;

    @ApiOperation(value = "分页查询", notes = "分页查询用户项目配置")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<ConfigDTO> configs(@PathVariable Long projectId, ConfigsPageVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<ConfigsPageVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            return nacosConfigsClient.fetchPage(clientEntity);
        });
    }

    @ApiOperation(value = "查询", notes = "查询用户项目配置详情")
    @GetMapping
    public Result<ConfigDTO> get(@PathVariable Long projectId, ConfigQueryVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigQueryVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            record.setTenant(record.getNamespaceId());
            return nacosConfigsClient.fetch(clientEntity);
        });
    }

    @ApiOperation(value = "添加", notes = "添加用户项目配置")
    @PostMapping
    public Result<Boolean> add(@PathVariable Long projectId, @RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            return nacosConfigsClient.add(clientEntity);
        });
    }

    @ApiOperation(value = "修改", notes = "修改用户项目配置")
    @PutMapping
    public Result<Boolean> edit(@PathVariable Long projectId,@RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            return nacosConfigsClient.edit(clientEntity);
        });
    }

    @ApiOperation(value = "克隆", notes = "克隆用户项目配置")
    @PostMapping("clone")
    public Result<?> clone(@PathVariable Long projectId, ConfigPolicyVO record,
                           @RequestBody ConfigCloneVO[] records) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigPolicyVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            return nacosConfigsClient.clone(clientEntity, records);
        });
    }

    @ApiOperation(value = "删除", notes = "删除用户项目配置")
    @DeleteMapping
    public Result<Boolean> delete(@PathVariable Long projectId, ConfigDeleteVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigDeleteVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            return nacosConfigsClient.delete(clientEntity);
        });
    }

}
