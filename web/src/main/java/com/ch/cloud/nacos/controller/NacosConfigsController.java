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
    public PageResult<ConfigDTO> page(ConfigsPageClientVO record) {
        
        return ResultUtils.wrapPage(() -> {
            ClientEntity<ConfigsPageClientVO> entity = nacosNamespaceValidator.valid(record);
            record.setTenant(record.getNamespaceId());
            record.setNamespaceId(null);
            return nacosConfigsClient.fetchPage(entity);
        });
    }
    
    @ApiOperation(value = "查询", notes = "查询配置详情")
    @GetMapping
    public Result<ConfigDTO> get(ConfigQueryClientVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigQueryClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            record.setTenant(record.getNamespaceId());
            return nacosConfigsClient.fetch(clientEntity);
        });
    }
    
    @ApiOperation(value = "添加", notes = "添加配置")
    @PostMapping
    public Result<Boolean> add(@RequestBody ConfigClientVO record) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();
            ClientEntity<ConfigClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            if (CommonUtils.isDecimal(record.getAppName())) {
                String group = nacosNamespaceValidator.fetchGroupId(Long.parseLong(record.getAppName()), nid);
                record.setGroup(group);
            }
            return nacosConfigsClient.add(clientEntity);
        });
    }
    
    @ApiOperation(value = "修改", notes = "修改配置")
    @PutMapping
    public Result<Boolean> edit(@RequestBody ConfigClientVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosConfigsClient.edit(clientEntity);
        });
    }
    
    @ApiOperation(value = "克隆", notes = "克隆配置")
    @PostMapping("clone")
    public Result<?> clone(ConfigPolicyClientVO record, @RequestBody ConfigCloneVO[] records) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigPolicyClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosConfigsClient.clone(clientEntity, records);
        });
    }
    
    @ApiOperation(value = "删除", notes = "删除配置")
    @DeleteMapping
    public Result<Boolean> delete(ConfigDeleteClientVO record) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigDeleteClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosConfigsClient.delete(clientEntity);
        });
    }
    
    /**
     * export=true&tenant=&group=&appName=&ids=61&namespaceId=2
     *
     * @param record
     * @return
     */
    @ApiOperation(value = "导出配置", notes = "导出配置")
    @GetMapping("export")
    public ResponseEntity<Resource> export(ConfigExportClientVO record) {
        AtomicReference<ClientEntity<ConfigExportClientVO>> clientEntity = new AtomicReference<>();
        Result<Object> result = ResultUtils.wrap(() -> clientEntity.set(nacosNamespaceValidator.valid(record)));
        if (result.isSuccess()) {
            return nacosConfigsClient.export(clientEntity.get());
        }
        return ResponseEntity.badRequest().build();
    }
    
    
    @ApiOperation(value = "导入配置", notes = "导入配置")
    @PostMapping("import")
    public Result<?> importZip(ConfigImportClientVO record, @RequestPart("file") MultipartFile file) {
        return ResultUtils.wrapFail(() -> {
            ClientEntity<ConfigImportClientVO> clientEntity = nacosNamespaceValidator.valid(record);
            return nacosConfigsClient.importZip(clientEntity, file);
        });
        
    }
}
