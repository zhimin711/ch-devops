package com.ch.cloud.nacos.controller;

import com.ch.cloud.devops.domain.Namespace;
import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.nacos.service.INacosNamespaceProjectService;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.cloud.types.NamespaceType;
import com.ch.cloud.upms.client.UpmsProjectClientService;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.pojo.VueRecord;
import com.ch.pojo.VueRecord2;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.VueRecordUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "Nacos项目服务")
@RequestMapping("/nacos/projects")
public class NacosProjectsController {

    @Autowired
    private UpmsProjectClientService upmsProjectClientService;

    @Autowired
    private INacosNamespaceProjectService namespaceProjectService;
    @Autowired
    private INamespaceService    namespaceService;
    @Autowired
    private INacosClusterService nacosClusterService;

    @ApiOperation(value = "分页查询", notes = "分页查询项目")
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<ProjectDto> page(ProjectDto record,
                                       @PathVariable(value = "num") int pageNum,
                                       @PathVariable(value = "size") int pageSize) {
        return upmsProjectClientService.page(pageNum, pageSize, record.getCode(), record.getName(), record.getTenantName());
//        PageInfo<ProjectDto> page = nacosClusterService.findPage(record, pageNum, pageSize);

    }


    @GetMapping({"{id:[0-9]+}/namespaces"})
    public Result<VueRecord> findNamespaces(@PathVariable Long id) {
        return ResultUtils.wrapList(() -> {
            List<Long> namespaceIds = namespaceProjectService.findNamespaceIdsByProjectId(id);
            List<Namespace> namespaces = namespaceService.findByPrimaryKeys(namespaceIds);
            return VueRecordUtils.covertIdList(namespaces);
        });
    }


    @PostMapping({"{id:[0-9]+}/namespaces"})
    public Result<Integer> saveProjectNamespaces(@PathVariable Long id, @RequestBody List<Long> namespaceIds) {
        return ResultUtils.wrap(() -> namespaceProjectService.assignProjectNamespaces(id, namespaceIds));
    }

    @GetMapping({"{id:[0-9]+}/clusters"})
    public Result<VueRecord> listCluster(@PathVariable Long id) {
        return ResultUtils.wrap(() -> {
            List<Long> clusterIds = namespaceProjectService.findClusterIdsByProjectIdAndNamespaceType(id, NamespaceType.NACOS);
            List<NacosCluster> clusters = nacosClusterService.findByPrimaryKeys(clusterIds);
            return VueRecordUtils.covertIdList(clusters);
        });
    }

    @ApiOperation(value = "查询项目nacos集群空间列表", notes = "查询项目nacos集群空间列表")
    @GetMapping(value = {"{projectId:[0-9]+}/namespaces/{clusterId:[0-9]+}"})
    public Result<VueRecord2> projectClusterNamespaces(@PathVariable Long projectId, @PathVariable Long clusterId) {
        return ResultUtils.wrap(() -> {
            List<NamespaceDto> records = namespaceProjectService.findNamespacesByProjectIdAndClusterId(projectId, clusterId);
            return records.stream().map(e -> {
                VueRecord2 record = new VueRecord2();
                record.setValue(e.getId().toString());
                record.setLabel(e.getName());
                return record;
            }).collect(Collectors.toList());
        });
    }
}
