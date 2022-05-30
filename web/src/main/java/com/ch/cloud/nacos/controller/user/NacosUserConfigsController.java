package com.ch.cloud.nacos.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.ch.cloud.nacos.client.NacosConfigsClient;
import com.ch.cloud.nacos.dto.ConfigDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.*;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
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
            String nid = record.getNamespaceId();

            ClientEntity<ConfigsPageVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            record.setTenant(record.getNamespaceId());

            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            record.setGroup(groupId);
            if (CommonUtils.isNotEmpty(groupId)) {
                record.setAppName(null);
            }
            return nacosConfigsClient.fetchPage(clientEntity);
        });
    }

    @ApiOperation(value = "查询", notes = "查询用户项目配置详情")
    @GetMapping
    public Result<ConfigDTO> get(@PathVariable Long projectId, ConfigQueryVO record) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();

            ClientEntity<ConfigQueryVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            record.setTenant(record.getNamespaceId());

            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            record.setGroup(groupId);
            return nacosConfigsClient.fetch(clientEntity);
        });
    }

    @ApiOperation(value = "添加", notes = "添加用户项目配置")
    @PostMapping
    public Result<Boolean> add(@PathVariable Long projectId, @RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();
            ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            record.setGroup(groupId);
            return nacosConfigsClient.add(clientEntity);
        });
    }

    @ApiOperation(value = "修改", notes = "修改用户项目配置")
    @PutMapping
    public Result<Boolean> edit(@PathVariable Long projectId, @RequestBody ConfigVO record) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();
            ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            record.setGroup(groupId);
            return nacosConfigsClient.edit(clientEntity);
        });
    }

    @ApiOperation(value = "克隆", notes = "克隆用户项目配置")
    @PostMapping("clone")
    public Result<?> clone(@PathVariable Long projectId, ConfigPolicyVO record,
                           @RequestBody ConfigCloneVO[] records) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();
            ClientEntity<ConfigPolicyVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            for (ConfigCloneVO cloneVO : records) {
                cloneVO.setGroup(groupId);
            }
            return nacosConfigsClient.clone(clientEntity, records);
        });
    }

    @ApiOperation(value = "删除", notes = "删除用户项目配置")
    @DeleteMapping
    public Result<Boolean> delete(@PathVariable Long projectId, ConfigDeleteVO record) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();
            ClientEntity<ConfigDeleteVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            record.setGroup(groupId);
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

    @ApiOperation(value = "查询", notes = "查询配置详情")
    @PutMapping("rollback")
    public Result<Boolean> rollback(@PathVariable Long projectId, @RequestParam String opType, @RequestBody HistoryRollbackVO record) {
        return ResultUtils.wrapFail(() -> {
            switch (opType) {
                case "I":
                    return delete(projectId, record);
                case "D":
                case "U":
                    return save(projectId, record);
                default:
                    ExceptionUtils._throw(PubError.NOT_ALLOWED, "非法操作");
            }
            return false;
        });
    }

    private Boolean save(Long projectId, HistoryRollbackVO record) {
        ConfigVO configVO = new ConfigVO();
        BeanUtil.copyProperties(record, configVO);
        ClientEntity<ConfigVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, configVO);
        return nacosConfigsClient.add(clientEntity);
    }

    private Boolean delete(Long projectId, HistoryRollbackVO record) {
        ConfigDeleteVO deleteVO = new ConfigDeleteVO();
        BeanUtil.copyProperties(record, deleteVO);
        ClientEntity<ConfigDeleteVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, deleteVO);
        return nacosConfigsClient.delete(clientEntity);
    }
}
