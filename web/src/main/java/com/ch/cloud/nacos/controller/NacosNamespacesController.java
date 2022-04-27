package com.ch.cloud.nacos.controller;


import com.ch.cloud.nacos.client.NacosNamespacesClient;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.domain.Namespace;
import com.ch.cloud.nacos.dto.NacosNamespace;
import com.ch.cloud.nacos.dto.NamespaceDto;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.nacos.service.INamespaceService;
import com.ch.cloud.types.NamespaceType;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.pojo.VueRecord;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.UUIDGenerator;
import com.ch.utils.BeanUtilsV2;
import com.ch.utils.CommonUtils;
import com.ch.utils.VueRecordUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 关系-命名空间表 前端控制器
 * </p>
 *
 * @author zhimin.ma
 * @since 2021-09-28
 */
@RestController
@RequestMapping("/nacos/namespaces")
public class NacosNamespacesController {

    @Autowired
    private INamespaceService    namespaceService;
    @Autowired
    private INacosClusterService nacosClusterService;
//    @Autowired
//    private IProjectService      projectService;

    @Autowired
    private NacosNamespacesClient nacosNamespacesClient;


    @ApiOperation(value = "分页查询", notes = "分页查询命名空间")
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<Namespace> page(Namespace record,
                                      @PathVariable(value = "num") int pageNum,
                                      @PathVariable(value = "size") int pageSize) {
        record.setType(NamespaceType.NACOS);
        return ResultUtils.wrapPage(() -> namespaceService.findPage2(record, pageNum, pageSize));
    }


    @ApiOperation(value = "添加", notes = "添加命名空间")
    @PostMapping
    public Result<Integer> add(@RequestBody Namespace record) {
        return ResultUtils.wrapFail(() -> {
            checkSaveOrUpdate(record);
            record.setUid(UUIDGenerator.generateUid().toString());
            boolean syncOk = nacosNamespacesClient.add(record);
            if (!syncOk) {
                ExceptionUtils._throw(PubError.CONNECT, "create nacos namespace failed!");
            }
            return namespaceService.save(record);
        });
    }

    @ApiOperation(value = "修改", notes = "修改命名空间")
    @PutMapping({"{id:[0-9]+}"})
    public Result<Integer> edit(@RequestBody Namespace record) {
        return ResultUtils.wrapFail(() -> {
            checkSaveOrUpdate(record);
            boolean syncOk = nacosNamespacesClient.edit(record);
            if (!syncOk) {
                ExceptionUtils._throw(PubError.CONNECT, "update nacos namespace failed!");
            }
            return namespaceService.update(record);
        });
    }

    private void checkSaveOrUpdate(Namespace record) {
        record.setType(NamespaceType.NACOS);
        if (record.getId() != null) {
            Namespace orig = namespaceService.find(record.getId());
            record.setClusterId(orig.getClusterId());
            record.setUid(orig.getUid());
        }
    }

    @GetMapping({"{id:[0-9]+}"})
    public Result<NamespaceDto> find(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            Namespace namespace = namespaceService.find(id);
            if (namespace == null) return null;
            NacosCluster cluster = nacosClusterService.find(namespace.getClusterId());
            NamespaceDto dto = BeanUtilsV2.clone(namespace, NamespaceDto.class);
            namespace.setAddr(cluster.getUrl());
            NacosNamespace nn = nacosNamespacesClient.fetch(namespace);
            if (nn != null) {
                dto.setConfigCount(nn.getConfigCount());
                dto.setQuota(nn.getQuota());
            }
            return dto;
        });
    }


    @ApiOperation(value = "删除", notes = "删除命名空间")
    @DeleteMapping({"{id:[0-9]+}"})
    public Result<Integer> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            Namespace orig = namespaceService.find(id);
            nacosNamespacesClient.delete(orig);
            return namespaceService.delete(id);
        });
    }

    @ApiOperation(value = "同步", notes = "同步-NACOS命名空间")
    @GetMapping({"/sync/{clusterId}"})
    public Result<Boolean> sync(@PathVariable Long clusterId) {
        return ResultUtils.wrapFail(() -> {
            NacosCluster cluster = nacosClusterService.find(clusterId);
            ExceptionUtils.assertEmpty(cluster, PubError.CONFIG, "nacos address");
            List<NacosNamespace> list = nacosNamespacesClient.fetchAll(cluster.getUrl());
            if (CommonUtils.isNotEmpty(list)) {
                saveNacosNamespaces(list, clusterId);
            }
            return true;
        });
    }

    private void saveNacosNamespaces(List<NacosNamespace> list, Long clusterId) {
        if (list.isEmpty()) return;
        list.forEach(e -> {
            Namespace record = new Namespace();
            record.setClusterId(clusterId);
            record.setType(NamespaceType.NACOS);
            record.setUid(e.getNamespace());
            record.setName(e.getNamespaceShowName());
            Namespace orig = namespaceService.findByUid(e.getNamespace());
            if (orig != null) {
                orig.setName(e.getNamespaceShowName());
                namespaceService.update(orig);
            } else {
                namespaceService.save(record);
            }
        });
    }


    @GetMapping({"{id}/projects"})
    public Result<VueRecord> findProjects(@PathVariable Long id, @RequestParam(value = "s", required = false) String name) {
        return ResultUtils.wrapList(() -> {
//            List<Project> projects = projectService.findByNamespaceIdAndName(id, name);
            return VueRecordUtils.covertIdList(null);
        });
    }
}

