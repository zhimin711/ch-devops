package com.ch.cloud.nacos.controller;

import com.ch.cloud.devops.domain.Namespace;
import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.dto.ProjectNamespaceDTO;
import com.ch.cloud.devops.vo.ProjectNamespaceVO;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.nacos.service.INacosNamespaceProjectService;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.cloud.types.NamespaceType;
import com.ch.cloud.upms.client.UpmsProjectClientService;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.e.PubError;
import com.ch.pojo.VueRecord;
import com.ch.pojo.VueRecord2;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.AssertUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.VueRecordUtils;
import com.github.pagehelper.PageHelper;
import io.swagger.v3.oas.annotations.Operation; // 替换 io.swagger.annotations.ApiOperation
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    private UpmsProjectClientService upmsProjectClientService;

    @Autowired
    private INacosNamespaceProjectService nacosNamespaceProjectService;
    @Autowired
    private INamespaceService             namespaceService;
    @Autowired
    private INacosClusterService          nacosClusterService;

    @Operation(summary = "分页查询", description = "分页查询项目") // 替换 @ApiOperation
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<ProjectDto> page(ProjectDto record,
                                       @PathVariable(value = "num") int pageNum,
                                       @PathVariable(value = "size") int pageSize) {
//        PageInfo<ProjectDto> page = nacosClusterService.findPage(record, pageNum, pageSize);
        return upmsProjectClientService.page(pageNum, pageSize, record.getCode(), record.getName(), record.getTenantName());
    }

    @Operation(summary = "查询项目nacos集群空间列表", description = "查询项目nacos集群空间列表") // 替换 @ApiOperation
    @GetMapping({"{projectId:[0-9]+}/{clusterId:[0-9]+}/namespaces"})
    public Result<ProjectNamespaceDTO> findNamespaces(@PathVariable Long projectId, @PathVariable Long clusterId) {
        return ResultUtils.wrapList(() -> nacosNamespaceProjectService.findByProjectIdAndClusterId(projectId, clusterId));
    }


    @Operation(summary = "保存项目nacos集群空间", description = "保存项目nacos集群空间") // 替换 @ApiOperation
    @PostMapping({"{projectId:[0-9]+}/{clusterId:[0-9]+}/namespaces"})
    public Result<Integer> saveProjectNamespaces(@PathVariable Long projectId, @PathVariable Long clusterId, @RequestBody List<ProjectNamespaceVO> namespaceVOS) {
        return ResultUtils.wrap(() -> {
            if (CommonUtils.isNotEmpty(namespaceVOS)) {
                Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
                AssertUtils.isTrue(result.isEmpty(), PubError.NOT_EXISTS, "projectId: " + projectId);
            }
            return nacosNamespaceProjectService.assignProjectNamespaces(projectId, clusterId, namespaceVOS);
        });
    }

    @Operation(summary = "列出集群", description = "列出集群") // 替换 @ApiOperation
    @GetMapping({"{id:[0-9]+}/clusters"})
    public Result<VueRecord> listCluster(@PathVariable Long id) {
        return ResultUtils.wrap(() -> {
            List<Long> clusterIds = nacosNamespaceProjectService.findClusterIdsByProjectIdAndNamespaceType(id, NamespaceType.NACOS);
            PageHelper.orderBy("sort, id asc");
            List<NacosCluster> clusters = nacosClusterService.findByPrimaryKeys(clusterIds);
            return VueRecordUtils.covertIdList(clusters);
        });
    }

}