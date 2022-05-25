package com.ch.cloud.nacos.controller.user;

import com.ch.cloud.nacos.client.NacosConfigsClient;
import com.ch.cloud.nacos.dto.ConfigDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.*;
import com.ch.cloud.upms.client.UpmsProjectClientService;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.atomic.AtomicReference;

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
    private NacosConfigsClient       nacosConfigsClient;
    @Autowired
    private NacosNamespaceValidator  nacosNamespaceValidator;
    @Autowired
    private UpmsProjectClientService upmsProjectClientService;

    @ApiOperation(value = "分页查询", notes = "分页查询用户项目配置")
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<ConfigDTO> configs(@PathVariable Long projectId, ConfigsPageVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<ConfigsPageVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
            record.setTenant(record.getNamespaceId());
            record.setGroup(result.get().getCode());
            return nacosConfigsClient.fetchPage(clientEntity);
        });
    }

    @ApiOperation(value = "查询", notes = "查询用户项目配置详情")
    @GetMapping
    public Result<ConfigDTO> get(@PathVariable Long projectId, ConfigQueryVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigQueryVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            record.setTenant(record.getNamespaceId());
//            record.setAppName();
            return nacosConfigsClient.fetch(clientEntity);
        });
    }

    @ApiOperation(value = "添加", notes = "添加用户项目配置")
    @PostMapping
    public Result<Boolean> add(@PathVariable Long projectId, @RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
            record.setGroup(result.get().getCode());
            return nacosConfigsClient.add(clientEntity);
        });
    }

    @ApiOperation(value = "修改", notes = "修改用户项目配置")
    @PutMapping
    public Result<Boolean> edit(@PathVariable Long projectId, @RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
            record.setGroup(result.get().getCode());
            return nacosConfigsClient.edit(clientEntity);
        });
    }

    @ApiOperation(value = "克隆", notes = "克隆用户项目配置")
    @PostMapping("clone")
    public Result<?> clone(@PathVariable Long projectId, ConfigPolicyVO record,
                           @RequestBody ConfigCloneVO[] records) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigPolicyVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
            for (ConfigCloneVO cloneVO : records) {
                cloneVO.setGroup(result.get().getCode());
            }
            return nacosConfigsClient.clone(clientEntity, records);
        });
    }

    @ApiOperation(value = "删除", notes = "删除用户项目配置")
    @DeleteMapping
    public Result<Boolean> delete(@PathVariable Long projectId, ConfigDeleteVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigDeleteVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
            record.setGroup(result.get().getCode());
            return nacosConfigsClient.delete(clientEntity);
        });
    }


    @ApiOperation(value = "导出项目配置", notes = "导出项目配置")
    @GetMapping("export")
    public ResponseEntity<Resource> export(@PathVariable Long projectId, ConfigExportVO record) {
        AtomicReference<ClientEntity<ConfigExportVO>> clientEntity = new AtomicReference<>();
        Result<Object> result = ResultUtils.wrap(() -> clientEntity.set(nacosNamespaceValidator.validUserNamespace(projectId, record)));
        if (result.isSuccess()) {
            return nacosConfigsClient.export(clientEntity.get());
        }
        return ResponseEntity.badRequest().build();
    }


    @ApiOperation(value = "导入项目配置", notes = "导入项目配置")
    @PostMapping("import")
    public Result<?> importZip(@PathVariable Long projectId, ConfigImportVO record, @RequestPart("file") MultipartFile file) {
        return ResultUtils.wrap(() -> {
            ClientEntity<ConfigImportVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            return nacosConfigsClient.importZip(clientEntity, file);
        });

    }

}
