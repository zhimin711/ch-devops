package com.ch.cloud.nacos.controller.user;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.ch.cloud.devops.enums.Permission;
import com.ch.cloud.web.annotation.OriginalReturn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.nacos.client.NacosConfigsClient;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.dto.ConfigDTO;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.ConfigCloneVO;
import com.ch.cloud.nacos.vo.ConfigDeleteClientVO;
import com.ch.cloud.nacos.vo.ConfigExportClientVO;
import com.ch.cloud.nacos.vo.ConfigImportClientVO;
import com.ch.cloud.nacos.vo.ConfigPolicyClientVO;
import com.ch.cloud.nacos.vo.ConfigQueryClientVO;
import com.ch.cloud.nacos.vo.ConfigClientVO;
import com.ch.cloud.nacos.vo.ConfigsPageClientVO;
import com.ch.cloud.nacos.vo.HistoryRollbackClientVO;
import com.ch.cloud.types.NamespaceType;
import com.ch.e.ExUtils;
import com.ch.e.PubError;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation; // 替换 io.swagger.annotations.ApiOperation
import lombok.extern.slf4j.Slf4j;

/**
 * 描述：配置
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Tag(name = "Nacos用户配置服务")
@RestController
@RequestMapping("/nacos/user/{projectId:[0-9]+}/configs")
@Slf4j
public class NacosUserConfigsController {
    
    @Autowired
    private NacosConfigsClient nacosConfigsClient;
    
    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    
    @Autowired
    private IUserNamespaceService userNamespaceService;
    
    @Autowired
    private INacosClusterService nacosClusterService;
    
    @Operation(summary = "分页查询", description = "分页查询用户项目配置") // 替换 @ApiOperation
    @GetMapping(value = {"{pageNo:[0-9]+}/{pageSize:[0-9]+}"})
    public PageResult<ConfigDTO> configs(@PathVariable Long projectId, @PathVariable Integer pageNo,
            @PathVariable Integer pageSize, ConfigsPageClientVO record) {
        return ResultUtils.wrapPage(() -> {
            String nid = record.getNamespaceId();
            record.setPageNo(pageNo);
            record.setPageSize(pageSize);
            ClientEntity<ConfigsPageClientVO> clientEntity = nacosNamespaceValidator.validUserNamespacePermission(
                    projectId, record, Permission.R);
            record.setTenant(record.getNamespaceId());
            
            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            record.setGroup(groupId);
            if (CommonUtils.isNotEmpty(groupId)) {
                record.setAppName(null);
            }
            return nacosConfigsClient.fetchPage(clientEntity);
        });
    }
    
    @Operation(summary = "查询", description = "查询用户项目配置详情") // 替换 @ApiOperation
    @GetMapping
    public Result<ConfigDTO> get(@PathVariable Long projectId, ConfigQueryClientVO record) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();
            
            ClientEntity<ConfigQueryClientVO> clientEntity = nacosNamespaceValidator.validUserNamespacePermission(
                    projectId, record, Permission.R);
            record.setTenant(record.getNamespaceId());
            
            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            record.setGroup(groupId);
            return nacosConfigsClient.fetch(clientEntity);
        });
    }
    
    @Operation(summary = "查询比较配置列表", description = "查询比较配置列表") // 替换 @ApiOperation
    @GetMapping("listCompare")
    public Result<ConfigDTO> listCompare(@PathVariable Long projectId, ConfigQueryClientVO record) {
        return ResultUtils.wrap(() -> {
            List<ConfigDTO> configs = Lists.newArrayList();
            List<NamespaceDto> namespaces = userNamespaceService.findNamespacesByUsernameAndProjectIdAndNamespaceType(
                    ContextUtil.getUsername(), projectId, NamespaceType.NACOS);
            if (namespaces.isEmpty()) {
                return null;
            }
            String currNamespace = record.getNamespaceId();
            record.setShow("all");
            namespaces.forEach(e -> {
                String nid = e.getId() + "";
                if (CommonUtils.isEquals(nid, currNamespace)) {
                    return;
                }
                record.setNamespaceId(nid);
                try {
                    ClientEntity<ConfigQueryClientVO> clientEntity = nacosNamespaceValidator.validUserNamespace(
                            projectId, record);
                    record.setTenant(record.getNamespaceId());
                    
                    String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
                    record.setGroup(groupId);
                    ConfigDTO config = nacosConfigsClient.fetch(clientEntity);
                    if (config != null) {
                        config.setTenant(e.getName());
                        NacosCluster cluster = nacosClusterService.find(e.getClusterId());
                        if (cluster != null) {
                            config.setAppName(cluster.getName() + "-" + cluster.getDescription());
                        }
                        configs.add(config);
                    }
                } catch (Exception ex) {
                    log.error("fetch compare config error!", ex);
                }
            });
            return configs;
        });
    }
    
    @Operation(summary = "添加", description = "添加用户项目配置") // 替换 @ApiOperation
    @PostMapping
    public Result<Boolean> add(@PathVariable Long projectId, @RequestBody ConfigClientVO record) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();
            ClientEntity<ConfigClientVO> clientEntity = nacosNamespaceValidator.validUserNamespacePermission(projectId,
                    record, Permission.W);
            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            record.setGroup(groupId);
            return nacosConfigsClient.add(clientEntity);
        });
    }
    
    @Operation(summary = "修改", description = "修改用户项目配置") // 替换 @ApiOperation
    @PutMapping
    public Result<Boolean> edit(@PathVariable Long projectId, @RequestBody ConfigClientVO record) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();
            ClientEntity<ConfigClientVO> clientEntity = nacosNamespaceValidator.validUserNamespacePermission(projectId,
                    record, Permission.W);
            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            record.setGroup(groupId);
            return nacosConfigsClient.edit(clientEntity);
        });
    }
    
    @Operation(summary = "克隆", description = "克隆用户项目配置") // 替换 @ApiOperation
    @PostMapping("clone")
    public Result<?> clone(@PathVariable Long projectId, ConfigPolicyClientVO record,
            @RequestBody ConfigCloneVO[] records) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();
            ClientEntity<ConfigPolicyClientVO> clientEntity = nacosNamespaceValidator.validUserNamespacePermission(
                    projectId, record, Permission.W);
            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            for (ConfigCloneVO cloneVO : records) {
                cloneVO.setGroup(groupId);
            }
            return nacosConfigsClient.clone(clientEntity, records);
        });
    }
    
    @Operation(summary = "删除", description = "删除用户项目配置") // 替换 @ApiOperation
    @DeleteMapping
    public Result<Boolean> delete(@PathVariable Long projectId, ConfigDeleteClientVO record) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();
            ClientEntity<ConfigDeleteClientVO> clientEntity = nacosNamespaceValidator.validUserNamespacePermission(
                    projectId, record, Permission.W);
            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            record.setGroup(groupId);
            return nacosConfigsClient.delete(clientEntity);
        });
    }
    
    @Operation(summary = "导出项目配置", description = "导出项目配置") // 替换 @ApiOperation
    @GetMapping("export")
    @OriginalReturn
    public ResponseEntity<Resource> export(@PathVariable Long projectId, ConfigExportClientVO record) {
        String nid = record.getNamespaceId();
        AtomicReference<ClientEntity<ConfigExportClientVO>> clientEntity = new AtomicReference<>();
        Result<Object> result = ResultUtils.wrap(() -> clientEntity.set(
                nacosNamespaceValidator.validUserNamespacePermission(projectId, record, Permission.R)));
        if (result.isSuccess()) {
            record.setTenant(record.getNamespaceId());
            String groupId = nacosNamespaceValidator.fetchGroupId(projectId, nid);
            if (CommonUtils.isNotEmpty(groupId)) {
                record.setAppName(null);
                record.setGroup(groupId);
            }
            return nacosConfigsClient.export(clientEntity.get());
        }
        return ResponseEntity.badRequest().build();
    }
    
    @Operation(summary = "导入项目配置", description = "导入项目配置") // 替换 @ApiOperation
    @PostMapping("import")
    public Result<?> importZip(@PathVariable Long projectId, ConfigImportClientVO record,
            @RequestPart("file") MultipartFile file) {
        return ResultUtils.wrap(() -> {
            ClientEntity<ConfigImportClientVO> clientEntity = nacosNamespaceValidator.validUserNamespacePermission(
                    projectId, record, Permission.W);
            return nacosConfigsClient.importZip(clientEntity, file);
        });
        
    }
    
    @Operation(summary = "查询", description = "查询配置详情") // 替换 @ApiOperation
    @PutMapping("rollback")
    public Result<Boolean> rollback(@PathVariable Long projectId, @RequestParam String opType,
            @RequestBody HistoryRollbackClientVO record) {
        return ResultUtils.wrapFail(() -> {
            switch (opType) {
                case "I":
                    return delete(projectId, record);
                case "D":
                case "U":
                    return save(projectId, record);
                default:
                    ExUtils.throwError(PubError.NOT_ALLOWED, "非法操作");
            }
            return false;
        });
    }
    
    private Boolean save(Long projectId, HistoryRollbackClientVO record) {
        ConfigClientVO configVO = new ConfigClientVO();
        BeanUtil.copyProperties(record, configVO);
        ClientEntity<ConfigClientVO> clientEntity = nacosNamespaceValidator.validUserNamespacePermission(projectId,
                configVO, Permission.W);
        return nacosConfigsClient.add(clientEntity);
    }
    
    private Boolean delete(Long projectId, HistoryRollbackClientVO record) {
        ConfigDeleteClientVO deleteVO = new ConfigDeleteClientVO();
        BeanUtil.copyProperties(record, deleteVO);
        ClientEntity<ConfigDeleteClientVO> clientEntity = nacosNamespaceValidator.validUserNamespacePermission(
                projectId, deleteVO, Permission.W);
        return nacosConfigsClient.delete(clientEntity);
    }
}