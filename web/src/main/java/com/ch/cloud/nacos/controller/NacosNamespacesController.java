package com.ch.cloud.nacos.controller;

import com.ch.cloud.devops.dto.Namespace;
import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.cloud.nacos.client.NacosNamespacesClient;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.dto.NacosNamespaceDTO;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.nacos.service.INacosNamespaceProjectService;
import com.ch.cloud.types.NamespaceType;
import com.ch.cloud.upms.client.UpmsProjectClientService;
import com.ch.cloud.upms.client.UpmsTenantClientService;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.pojo.VueRecord;
import com.ch.pojo.VueRecord2;
import com.ch.result.InvokerPage;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.UUIDGenerator;
import com.ch.utils.AssertUtils;
import com.ch.utils.BeanUtilsV2;
import com.ch.utils.CommonUtils;
import com.ch.utils.VueRecordUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private INamespaceService namespaceService;
    @Autowired
    private INacosNamespaceProjectService nacosNamespaceProjectService;
    @Autowired
    private INacosClusterService     nacosClusterService;

    @Autowired
    private UpmsProjectClientService projectClientService;
    @Autowired
    private UpmsTenantClientService  tenantClientService;

    @Autowired
    private NacosNamespacesClient nacosNamespacesClient;


    @ApiOperation(value = "分页查询", notes = "分页查询命名空间")
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<Namespace> page(Namespace record,
                                      @PathVariable(value = "num") int pageNum,
                                      @PathVariable(value = "size") int pageSize) {
        record.setType(NamespaceType.NACOS);
        return ResultUtils.wrapPage(() -> {
            InvokerPage.Page<Namespace> page = namespaceService.invokerPage(record, pageNum, pageSize);
            if (page.getTotal() > 0) {
                page.getRows().forEach(e -> {
                    NacosCluster cluster = nacosClusterService.find(e.getClusterId());
                    e.setAddr(cluster.getUrl());

                    NacosNamespaceDTO r = ResultUtils.invoke(() -> nacosNamespacesClient.fetch(e));
                    if (r == null) return;
                    e.setConfigCount(r.getConfigCount());
                    e.setQuota(r.getQuota());
                });
            }
            return page;
        });
    }


    @ApiOperation(value = "添加", notes = "添加命名空间")
    @PostMapping
    public Result<Integer> add(@RequestBody Namespace record) {
        return ResultUtils.wrapFail(() -> {
            checkSaveOrUpdate(record);
            if (CommonUtils.isEmpty(record.getUid())) {
                record.setUid(UUIDGenerator.generateUid().toString());
            } else {
                NacosNamespaceDTO namespace = nacosNamespacesClient.fetch(record);
                AssertUtils.notNull(namespace, PubError.EXISTS,"id");
            }
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
        NacosCluster cluster = nacosClusterService.find(record.getClusterId());
        ExceptionUtils.assertEmpty(cluster, PubError.CONFIG, "nacos cluster" + record.getClusterId());
        record.setAddr(cluster.getUrl());
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
            if (namespace == null)
                return null;
            NacosCluster cluster = nacosClusterService.find(namespace.getClusterId());
            NamespaceDto dto = BeanUtilsV2.clone(namespace, NamespaceDto.class);
            namespace.setAddr(cluster.getUrl());
            NacosNamespaceDTO nn = nacosNamespacesClient.fetch(namespace);
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
            List<Long> projectIds = nacosNamespaceProjectService.findProjectIdsByNamespaceId(id);
            AssertUtils.isTrue(CommonUtils.isNotEmpty(projectIds), PubError.NOT_ALLOWED, "存在关联项目不允许删除");
            Namespace namespace = namespaceService.find(id);
            AssertUtils.isTrue(CommonUtils.isEmpty(namespace.getUid()), PubError.NOT_ALLOWED, "保留空间不允许删除");

            NacosCluster cluster = nacosClusterService.find(namespace.getClusterId());
            namespace.setAddr(cluster.getUrl());
            nacosNamespacesClient.delete(namespace);
            return namespaceService.delete(id);
        });
    }

    @ApiOperation(value = "同步", notes = "同步-NACOS命名空间")
    @PostMapping({"/sync/{clusterId}"})
    public Result<Boolean> sync(@PathVariable Long clusterId) {
        return ResultUtils.wrapFail(() -> {
            NacosCluster cluster = nacosClusterService.find(clusterId);
            ExceptionUtils.assertEmpty(cluster, PubError.CONFIG, "nacos cluster" + clusterId);
            List<NacosNamespaceDTO> list = nacosNamespacesClient.fetchAll(cluster.getUrl());
            List<Namespace> list2 = namespaceService.findByClusterIdAndName(clusterId, null);
            Map<String, NacosNamespaceDTO> nacosMap = CommonUtils.isNotEmpty(list) ? list.stream().collect(Collectors.toMap(NacosNamespaceDTO::getNamespace, e -> e)) : Maps.newHashMap();
            Map<String, Namespace> localMap = CommonUtils.isNotEmpty(list2) ? list2.stream().collect(Collectors.toMap(Namespace::getUid, e -> e)) : Maps.newHashMap();

            if (localMap.isEmpty()) {
                saveNacosNamespaces(list, clusterId);
            } else if (nacosMap.isEmpty()) {
                list2.forEach(nacosNamespacesClient::add);
            } else {
                List<NacosNamespaceDTO> newList = Lists.newArrayList();
                nacosMap.forEach((k, v) -> {
                    if (!localMap.containsKey(k)) newList.add(v);
                });
                saveNacosNamespaces(newList, clusterId);
                localMap.forEach((k, v) -> {
                    if (!nacosMap.containsKey(k)) nacosNamespacesClient.add(v);
                });
            }
            return true;
        });
    }

    private void saveNacosNamespaces(List<NacosNamespaceDTO> list, Long clusterId) {
        if (list.isEmpty())
            return;
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


    @GetMapping({"{id:[0-9]+}/projects"})
    public Result<VueRecord2> findProjects(@PathVariable Long id) {
        return ResultUtils.wrapList(() -> {
            List<Long> projectIds = nacosNamespaceProjectService.findProjectIdsByNamespaceId(id);
            Result<ProjectDto> projects = projectClientService.findByIds(projectIds);
            return VueRecordUtils.covert(projects.getRows());
        });
    }


    @PostMapping({"{id:[0-9]+}/projects"})
    public Result<Integer> saveProjectNamespaces(@PathVariable Long id, @RequestBody List<Long> projectIds) {
        return ResultUtils.wrap(() -> nacosNamespaceProjectService.assignNamespaceProjects(id, projectIds));
    }

}

