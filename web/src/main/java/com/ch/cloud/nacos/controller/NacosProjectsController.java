package com.ch.cloud.nacos.controller;

import com.ch.cloud.devops.dto.ProjectNamespaceDTO;
import com.ch.cloud.devops.dto.UserProjectNamespaceDto;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.devops.vo.ProjectNamespaceVO;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.nacos.service.INacosNamespaceProjectService;
import com.ch.cloud.types.NamespaceType;
import com.ch.cloud.upms.client.UpmsProjectClient;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.cloud.upms.dto.ProjectUserRoleDTO;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.pojo.VueRecord;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.VueRecordUtils;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 描述：Nacos项目服务
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@RestController
@Tag(name = "Nacos项目服务")
@RequestMapping("/nacos/projects")
public class NacosProjectsController {
    
    @Autowired
    private UpmsProjectClient upmsProjectClient;
    
    @Autowired
    private INacosNamespaceProjectService nacosNamespaceProjectService;
    
    @Autowired
    private INamespaceService namespaceService;
    
    @Autowired
    private INacosClusterService nacosClusterService;
    
    @Autowired
    private IUserNamespaceService userNamespaceService;
    
    @Operation(summary = "分页查询", description = "分页查询项目") // 替换 @ApiOperation
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<ProjectDto> page(ProjectDto record, @PathVariable(value = "num") int pageNum,
            @PathVariable(value = "size") int pageSize) {
        //        PageInfo<ProjectDto> page = nacosClusterService.findPage(record, pageNum, pageSize);
        return upmsProjectClient.page(pageNum, pageSize, record.getCode(), record.getName(), record.getTenantName());
    }
    
    @Operation(summary = "查询项目nacos集群空间列表", description = "查询项目nacos集群空间列表") // 替换 @ApiOperation
    @GetMapping({"{projectId:[0-9]+}/{clusterId:[0-9]+}/namespaces"})
    public Result<ProjectNamespaceDTO> findNamespaces(@PathVariable Long projectId, @PathVariable Long clusterId) {
        return ResultUtils.wrapList(
                () -> nacosNamespaceProjectService.findByProjectIdAndClusterId(projectId, clusterId));
    }
    
    
    @Operation(summary = "保存项目nacos集群空间", description = "保存项目nacos集群空间") // 替换 @ApiOperation
    @PostMapping({"{projectId:[0-9]+}/{clusterId:[0-9]+}/namespaces"})
    public Result<Integer> saveProjectNamespaces(@PathVariable Long projectId, @PathVariable Long clusterId,
            @RequestBody List<ProjectNamespaceVO> namespaceVOS) {
        return ResultUtils.wrap(() -> {
            if (CommonUtils.isNotEmpty(namespaceVOS)) {
                Result<ProjectDto> result = upmsProjectClient.infoByIdOrCode(projectId, null);
                Assert.notEmpty(result.getRows(), PubError.NOT_EXISTS, "projectId: " + projectId);
            }
            return nacosNamespaceProjectService.assignProjectNamespaces(projectId, clusterId, namespaceVOS);
        });
    }
    
    @Operation(summary = "列出集群", description = "列出集群") // 替换 @ApiOperation
    @GetMapping({"{id:[0-9]+}/clusters"})
    public Result<VueRecord> listCluster(@PathVariable Long id) {
        return ResultUtils.wrap(() -> {
            List<Long> clusterIds = nacosNamespaceProjectService.findClusterIdsByProjectIdAndNamespaceType(id,
                    NamespaceType.NACOS);
            PageHelper.orderBy("sort, id asc");
            List<NacosCluster> clusters = nacosClusterService.findByPrimaryKeys(clusterIds);
            return VueRecordUtils.covertIdList(clusters);
        });
    }
    
    @Operation(summary = "查询项目下用户列表", description = "查询项目下用户列表")
    @GetMapping({"{id:[0-9]+}/users"})
    public Result<ProjectUserRoleDTO> findUsers(@PathVariable Long id) {
        return upmsProjectClient.findUsers(id);
    }
    
    @Operation(summary = "查询项目下命名空间的用户及权限", description = "查询项目下命名空间的用户及权限")
    @GetMapping({"{id:[0-9]+}/user-permission"})
    public Result<UserProjectNamespaceDto> listUserPermissions(@PathVariable Long id, @RequestParam String username) {
        return ResultUtils.wrap(() -> {
            List<Long> clusterIds = nacosNamespaceProjectService.findClusterIdsByProjectIdAndNamespaceType(id,
                    NamespaceType.NACOS);
            Assert.notEmpty(clusterIds, PubError.NOT_EXISTS, "项目下没有命名空间");
            List<UserProjectNamespaceDto> all = Lists.newArrayList();
            clusterIds.forEach(clusterId -> {
                List<UserProjectNamespaceDto> list = userNamespaceService.listUserNamespacesByType(username, id,
                        clusterId, NamespaceType.NACOS);
                if (CommonUtils.isNotEmpty(list)) {
                    NacosCluster nacosCluster = nacosClusterService.find(clusterId);
                    list.forEach(e -> e.setClusterName(nacosCluster.getName()));
                    all.addAll(list);
                }
            });
            return all;
        });
    }
}