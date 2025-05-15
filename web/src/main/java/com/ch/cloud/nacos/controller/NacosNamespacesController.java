package com.ch.cloud.nacos.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ch.cloud.devops.vo.NamespaceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ch.cloud.devops.domain.Namespace;
import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.dto.ProjectNamespaceDTO;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.cloud.nacos.client.NacosNamespacesClient;
import com.ch.cloud.nacos.client.NacosUserClient;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.dto.NacosNamespaceDTO;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.nacos.service.INacosNamespaceProjectService;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.NacosNamespaceClientVO;
import com.ch.cloud.nacos.vo.NamespaceClientVO;
import com.ch.cloud.types.NamespaceType;
import com.ch.cloud.upms.client.UpmsProjectClientService;
import com.ch.cloud.upms.client.UpmsTenantClientService;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.e.ExUtils;
import com.ch.e.PubError;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Nacos命名空间服务")
public class NacosNamespacesController {

    @Autowired
    private INamespaceService namespaceService;
    @Autowired
    private INacosNamespaceProjectService nacosNamespaceProjectService;
    @Autowired
    private INacosClusterService nacosClusterService;

    @Autowired
    private UpmsProjectClientService projectClientService;
    @Autowired
    private UpmsTenantClientService tenantClientService;

    @Autowired
    private NacosNamespacesClient nacosNamespacesClient;
    @Autowired
    private NacosUserClient nacosUserClient;

    @Operation(summary = "分页查询", description = "分页查询命名空间")
    @GetMapping(value = {"{num:[0-9]+}/{size:[0-9]+}"})
    public PageResult<Namespace> page(@PathVariable(value = "num") int pageNum,
        @PathVariable(value = "size") int pageSize, NamespaceVO record) {
        record.setType(NamespaceType.NACOS);
        Namespace namespace = new Namespace();
        BeanUtils.copyProperties(record, namespace);
        return ResultUtils.wrapPage(() -> {
            InvokerPage.Page<Namespace> page = namespaceService.invokerPage(namespace, pageNum, pageSize);
            if (page.getTotal() > 0) {
                page.getRows().forEach(e -> {
                    NacosCluster cluster = nacosClusterService.find(e.getClusterId());
                    ClientEntity<NamespaceClientVO> clientEntity =
                        new ClientEntity<>(cluster, new NamespaceClientVO(e.getUid(), ""));
                    NacosNamespaceDTO r = ResultUtils.invoke(() -> {
                        nacosUserClient.login(clientEntity);
                        return nacosNamespacesClient.fetch(clientEntity);
                    });
                    if (r == null)
                        return;
                    e.setConfigCount(r.getConfigCount());
                    e.setQuota(r.getQuota());
                });
            }
            return page;
        });
    }

    @Operation(summary = "添加", description = "添加命名空间")
    @PostMapping
    public Result<Integer> add(@RequestBody Namespace record) {
        return ResultUtils.wrapFail(() -> {
            checkSaveOrUpdate(record);
            ClientEntity<NamespaceClientVO> clientEntity =
                new ClientEntity<>(record.getCluster(), new NamespaceClientVO());
            nacosUserClient.login(clientEntity);
            if (CommonUtils.isEmpty(record.getUid())) {
                record.setUid(UUIDGenerator.generateUid().toString());
            } else {
                clientEntity.getData().setNamespaceId(record.getUid());
                NacosNamespaceDTO namespace = nacosNamespacesClient.fetch(clientEntity);
                AssertUtils.notNull(namespace, PubError.EXISTS, "id" + record.getUid());
            }
            boolean syncOk = convertAndSave(record.getCluster(), clientEntity, record);;
            if (!syncOk) {
                ExUtils.throwError(PubError.CONNECT, "create nacos namespace failed!");
            }
            return namespaceService.save(record);
        });
    }

    @Operation(summary = "修改", description = "修改命名空间")
    @PutMapping({"{id:[0-9]+}"})
    public Result<Integer> edit(@RequestBody Namespace record) {
        return ResultUtils.wrapFail(() -> {
            checkSaveOrUpdate(record);
            NacosNamespaceClientVO namespaceVO = new NacosNamespaceClientVO();
            namespaceVO.setNamespaceId(record.getUid());
            namespaceVO.setName(record.getName());
            namespaceVO.setDesc(record.getDescription());
            ClientEntity<NacosNamespaceClientVO> clientEntity = new ClientEntity<>(record.getCluster(), namespaceVO);
            nacosUserClient.login(clientEntity);
            boolean syncOk = nacosNamespacesClient.edit(clientEntity);
            if (!syncOk) {
                ExUtils.throwError(PubError.CONNECT, "update nacos namespace failed!");
            }
            return namespaceService.update(record);
        });
    }

    private void checkSaveOrUpdate(Namespace record) {
        NacosCluster cluster = nacosClusterService.find(record.getClusterId());
        AssertUtils.isEmpty(cluster, PubError.CONFIG, "nacos cluster" + record.getClusterId());
        record.setCluster(cluster);
        record.setType(NamespaceType.NACOS);
        if (record.getId() != null) {
            Namespace orig = namespaceService.find(record.getId());
            record.setClusterId(orig.getClusterId());
            record.setUid(orig.getUid());
        }
    }

    @Operation(summary = "查询命名空间详细", description = "查询命名空间详细")
    @GetMapping({"{id:[0-9]+}"})
    public Result<NamespaceDto> find(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            Namespace namespace = namespaceService.find(id);
            if (namespace == null)
                return null;
            NacosCluster cluster = nacosClusterService.find(namespace.getClusterId());
            NamespaceDto dto = BeanUtilsV2.clone(namespace, NamespaceDto.class);
            namespace.setCluster(cluster);
            ClientEntity<NamespaceClientVO> clientEntity =
                new ClientEntity<>(cluster, new NamespaceClientVO(namespace.getUid(), ""));
            NacosNamespaceDTO nn = nacosNamespacesClient.fetch(clientEntity);
            if (nn != null) {
                dto.setConfigCount(nn.getConfigCount());
                dto.setQuota(nn.getQuota());
            }
            return dto;
        });
    }

    @Operation(summary = "删除", description = "删除命名空间")
    @DeleteMapping({"{id:[0-9]+}"})
    public Result<Integer> delete(@PathVariable Long id) {
        return ResultUtils.wrapFail(() -> {
            List<Long> projectIds = nacosNamespaceProjectService.findProjectIdsByNamespaceId(id);
            AssertUtils.isTrue(CommonUtils.isNotEmpty(projectIds), PubError.NOT_ALLOWED, "存在关联项目不允许删除");
            Namespace namespace = namespaceService.find(id);
            AssertUtils.isTrue(CommonUtils.isEmpty(namespace.getUid()), PubError.NOT_ALLOWED, "保留空间不允许删除");

            NacosCluster cluster = nacosClusterService.find(namespace.getClusterId());
            ClientEntity<NamespaceClientVO> clientEntity =
                new ClientEntity<>(cluster, new NamespaceClientVO(namespace.getUid(), ""));
            nacosUserClient.login(clientEntity);
            nacosNamespacesClient.delete(clientEntity);
            return namespaceService.delete(id);
        });
    }

    @Operation(summary = "同步", description = "同步-NACOS命名空间")
    @PostMapping({"/sync/{clusterId}"})
    public Result<Boolean> sync(@PathVariable Long clusterId) {
        return ResultUtils.wrapFail(() -> {
            NacosCluster cluster = nacosClusterService.find(clusterId);
            AssertUtils.isEmpty(cluster, PubError.CONFIG, "nacos cluster" + clusterId);
            ClientEntity<NamespaceClientVO> clientEntity = new ClientEntity<>(cluster, new NamespaceClientVO());
            nacosUserClient.login(clientEntity);
            List<NacosNamespaceDTO> list = nacosNamespacesClient.fetchAll(clientEntity);
            List<Namespace> list2 = namespaceService.findByClusterIdAndName(clusterId, null);
            Map<String, NacosNamespaceDTO> nacosMap = CommonUtils.isNotEmpty(list)
                ? list.stream().collect(Collectors.toMap(NacosNamespaceDTO::getNamespace, e -> e)) : Maps.newHashMap();
            Map<String, Namespace> localMap = CommonUtils.isNotEmpty(list2)
                ? list2.stream().collect(Collectors.toMap(Namespace::getUid, e -> e)) : Maps.newHashMap();

            if (localMap.isEmpty()) {
                saveNacosNamespaces(list, clusterId);
            } else if (nacosMap.isEmpty()) {
                list2.forEach(record -> convertAndSave(cluster, clientEntity, record));
            } else {
                List<NacosNamespaceDTO> newList = Lists.newArrayList();
                nacosMap.forEach((k, v) -> {
                    if (!localMap.containsKey(k))
                        newList.add(v);
                });
                saveNacosNamespaces(newList, clusterId);
                localMap.forEach((k, record) -> {
                    if (!nacosMap.containsKey(k)) {
                        convertAndSave(cluster, clientEntity, record);
                    }
                });
            }
            return true;
        });
    }

    private boolean convertAndSave(NacosCluster cluster, ClientEntity<NamespaceClientVO> clientEntity,
        Namespace record) {
        NacosNamespaceClientVO namespaceVO = new NacosNamespaceClientVO();
        namespaceVO.setNamespaceId(record.getUid());
        namespaceVO.setAccessToken(clientEntity.getData().getAccessToken());
        namespaceVO.setName(record.getName());
        namespaceVO.setDesc(record.getDescription());
        return nacosNamespacesClient.add(ClientEntity.build(cluster, namespaceVO));
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
            Namespace orig = namespaceService.findByUid(clusterId, e.getNamespace());
            if (orig != null) {
                orig.setName(e.getNamespaceShowName());
                namespaceService.update(orig);
            } else {
                namespaceService.save(record);
            }
        });
    }

    @Operation(summary = "查询命名空间下项目列表", description = "查询命名空间下项目列表")
    @GetMapping({"{id:[0-9]+}/projects"})
    public Result<VueRecord2> findProjects(@PathVariable Long id) {
        return ResultUtils.wrapList(() -> {
            List<ProjectNamespaceDTO> list = nacosNamespaceProjectService.findByNamespaceId(id);
            if (list.isEmpty())
                return null;
            Map<Long, List<ProjectNamespaceDTO>> projectMap =
                list.stream().collect(Collectors.groupingBy(ProjectNamespaceDTO::getProjectId));
            Result<ProjectDto> result = projectClientService.infoByIds(Lists.newArrayList(projectMap.keySet()));
            if (result.isEmpty()) {
                return null;
            }
            result.getRows().forEach(e -> {
                if (projectMap.containsKey(e.getId())) {
                    ProjectNamespaceDTO pn = projectMap.get(e.getId()).get(0);
                    if (CommonUtils.isNotEmpty(pn.getGroupId())) {
                        e.setCode(pn.getGroupId());
                    }
                }
            });
            return VueRecordUtils.covert(result.getRows());
        });
    }

    @Operation(summary = "修改命名空间下项目列表", description = "修改命名空间下项目列表")
    @PostMapping({"{id:[0-9]+}/projects"})
    public Result<Integer> saveProjectNamespaces(@PathVariable Long id, @RequestBody List<Long> projectIds) {
        return ResultUtils.wrap(() -> nacosNamespaceProjectService.assignNamespaceProjects(id, projectIds));
    }
}