package com.ch.cloud.nacos.controller.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.ch.cloud.devops.domain.Namespace;
import com.ch.cloud.devops.domain.NamespaceApplyRecord;
import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.service.INamespaceApplyRecordService;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.nacos.client.NacosHistoryClient;
import com.ch.cloud.nacos.client.NacosInstancesClient;
import com.ch.cloud.nacos.client.NacosServicesClient;
import com.ch.cloud.nacos.client.NacosSubscribesClient;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.dto.HistoryDTO;
import com.ch.cloud.nacos.dto.ServiceInstanceDTO;
import com.ch.cloud.nacos.dto.SubscriberDTO;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.nacos.service.INacosNamespaceProjectService;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.HistoryPageClientVO;
import com.ch.cloud.nacos.vo.HistoryQueryClientVO;
import com.ch.cloud.nacos.vo.InstancesPageClientVO;
import com.ch.cloud.nacos.vo.ServicesPageClientVO;
import com.ch.cloud.nacos.vo.SubscribesPageClientVO;
import com.ch.cloud.types.NamespaceType;
import com.ch.cloud.upms.client.UpmsProjectClientService;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.pojo.VueRecord;
import com.ch.pojo.VueRecord2;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.s.ApproveStatus;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.CommonUtils;
import com.ch.utils.VueRecordUtils;
import com.google.common.collect.Lists;

import io.swagger.annotations.ApiOperation;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@RestController
@RequestMapping("/nacos/user")
public class NacosUserController {

    @Autowired
    private INamespaceService namespaceService;
    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    @Autowired
    private IUserNamespaceService userNamespaceService;
    @Autowired
    private INamespaceApplyRecordService namespaceApplyRecordService;

    @Autowired
    private INacosNamespaceProjectService nacosNamespaceProjectService;
    @Autowired
    private INacosClusterService nacosClusterService;

    @Autowired
    private NacosHistoryClient nacosHistoryClient;
    @Autowired
    private NacosInstancesClient nacosInstancesClient;
    @Autowired
    private NacosSubscribesClient nacosSubscribesClient;
    @Autowired
    private NacosServicesClient nacosServicesClient;

    @Autowired
    private UpmsProjectClientService upmsProjectClientService;

    @ApiOperation(value = "查询空间列表", notes = "查询用户命名空间")
    @GetMapping(value = {"{projectId:[0-9]+}/{clusterId:[0-9]+}/namespaces"})
    public Result<VueRecord2> namespaces(@PathVariable Long projectId, @PathVariable Long clusterId) {
        return ResultUtils.wrap(() -> {
            List<NamespaceDto> records =
                userNamespaceService.findNamespacesByUsernameAndProjectIdAndClusterIdAndNamespaceType(
                    ContextUtil.getUsername(), projectId, clusterId, NamespaceType.NACOS);
            return records.stream().map(e -> {
                VueRecord2 record = new VueRecord2();
                record.setValue(e.getId().toString());
                record.setLabel(e.getName());
                record.setKey(e.getUid());
                return record;
            }).collect(Collectors.toList());
        });
    }

    @ApiOperation(value = "分页查询", notes = "分页查询用户项目配置历史")
    @GetMapping(value = {"{projectId:[0-9]+}/history"})
    public PageResult<HistoryDTO> history(@PathVariable Long projectId, HistoryPageClientVO record) {
        return ResultUtils.wrapPage(() -> {
            String nid = record.getNamespaceId();
            ClientEntity<HistoryPageClientVO> entity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            record.setTenant(record.getNamespaceId());
            record.setGroup(nacosNamespaceValidator.fetchGroupId(projectId, nid));
            return nacosHistoryClient.fetchPage(entity);
        });
    }

    @ApiOperation(value = "查询", notes = "查询配置详情")
    @GetMapping(value = {"{projectId:[0-9]+}/history/detail"})
    public Result<HistoryDTO> getHistoryDetail(@PathVariable Long projectId, HistoryQueryClientVO record) {
        return ResultUtils.wrapFail(() -> {
            String nid = record.getNamespaceId();
            ClientEntity<HistoryQueryClientVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            record.setTenant(record.getNamespaceId());
            record.setGroup(nacosNamespaceValidator.fetchGroupId(projectId, nid));
            return nacosHistoryClient.fetch(clientEntity);
        });
    }

    @ApiOperation(value = "分页查询", notes = "分页查询用户项目服务实例")
    @GetMapping(value = {"{projectId:[0-9]+}/instances"})
    public Result<ServiceInstanceDTO> instances(@PathVariable Long projectId, InstancesPageClientVO record) {
        return ResultUtils.wrap(() -> {
            ClientEntity<InstancesPageClientVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);

            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
            record.setServiceName(result.get().getCode());

            ClientEntity<ServicesPageClientVO> clientEntity3 = new ClientEntity<>();
            clientEntity3.setUrl(clientEntity.getUrl());
            ServicesPageClientVO servicesPageVO = new ServicesPageClientVO();
            servicesPageVO.setAccessToken(record.getAccessToken());
            servicesPageVO.setNamespaceId(record.getNamespaceId());
            servicesPageVO.setServiceNameParam(result.get().getCode());
            servicesPageVO.setWithInstances(true);
            clientEntity3.setData(servicesPageVO);
            List<ServiceInstanceDTO> serviceInstanceDTOS = nacosServicesClient.fetchList(clientEntity3);
            if (serviceInstanceDTOS.isEmpty())
                return Lists.newArrayList();
            return serviceInstanceDTOS.stream()
                .filter(e -> CommonUtils.isEquals(servicesPageVO.getServiceNameParam(), e.getServiceName()))
                .collect(Collectors.toList());
        });
    }

    @ApiOperation(value = "分页查询", notes = "分页查询用户项目配置")
    @GetMapping(value = {"{projectId:[0-9]+}/subscribers"})
    public PageResult<SubscriberDTO> subscribers(@PathVariable Long projectId, SubscribesPageClientVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<SubscribesPageClientVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
            record.setServiceName(result.get().getCode());
            return nacosSubscribesClient.fetchPage(clientEntity);
        });
    }

    @ApiOperation(value = "查询项目可申请空间列表", notes = "查询项目可申请空间列表")
    @GetMapping({"apply/{projectId:[0-9]+}/{clusterId:[0-9]+}/namespaces"})
    public Result<VueRecord> findApplyNamespaces(@PathVariable Long projectId, @PathVariable Long clusterId) {
        return ResultUtils.wrapList(() -> {
            List<NamespaceDto> records =
                nacosNamespaceProjectService.findNamespacesByProjectIdAndClusterId(projectId, clusterId);
            return VueRecordUtils.covertIdList(records);
        });
    }

    @PostMapping({"apply/{projectId:[0-9]+}/{clusterId:[0-9]+}/namespaces"})
    public Result<Boolean> apply(@PathVariable Long projectId, @PathVariable Long clusterId,
        @RequestBody List<Long> namespaceIds) {
        return ResultUtils.wrap(() -> {
            nacosNamespaceValidator.validProjectNamespace(projectId, namespaceIds);
            NamespaceApplyRecord record = new NamespaceApplyRecord();
            record.setCreateBy(ContextUtil.getUsername());
            record.setType(NamespaceType.NACOS.getCode());
            record.setDataKey(projectId + "-" + clusterId);
            record.setStatus(ApproveStatus.STAY.getCode() + "");
            List<NamespaceApplyRecord> list = namespaceApplyRecordService.find(record);
            if (!list.isEmpty()) {
                ExceptionUtils._throw(PubError.EXISTS, "已提交申请,请联系管理员审核！");
            }
            NacosCluster cluster = nacosClusterService.find(clusterId);
            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);

            JSONObject object = new JSONObject();
            object.put("userId", ContextUtil.getUsername());
            object.put("projectId", projectId);
            object.put("projectName", result.get().getName());
            object.put("clusterId", clusterId);
            object.put("clusterName", cluster.getName());
            object.put("namespaceIds", namespaceIds);

            List<String> names = Lists.newArrayList();
            for (Long nid : namespaceIds) {
                Namespace n = namespaceService.find(nid);
                names.add(n.getName());
            }
            object.put("namespaceNames", String.join("|", names));

            record.setContent(object.toJSONString());

            return namespaceApplyRecordService.save(record) > 0;
        });
    }
}
