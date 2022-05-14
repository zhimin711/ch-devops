package com.ch.cloud.nacos.controller;

import com.ch.cloud.nacos.domain.Namespace;
import com.ch.cloud.nacos.service.INamespaceProjectService;
import com.ch.cloud.nacos.service.INamespaceService;
import com.ch.cloud.upms.client.UpmsProjectClientService;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.pojo.VueRecord;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.VueRecordUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private INamespaceProjectService namespaceProjectService;
    @Autowired
    private INamespaceService        namespaceService;

    @ApiOperation(value = "分页查询", notes = "分页查询nacos集群")
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<ProjectDto> page(ProjectDto record,
                                       @PathVariable(value = "num") int pageNum,
                                       @PathVariable(value = "size") int pageSize) {
      return   upmsProjectClientService.page(record,pageNum,pageSize);
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
}
