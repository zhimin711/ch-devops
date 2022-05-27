package com.ch.cloud.nacos.controller.user;

import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.devops.domain.Namespace;
import com.ch.cloud.devops.domain.NamespaceApplyRecord;
import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.service.INamespaceApplyRecordService;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.nacos.client.NacosHistoryClient;
import com.ch.cloud.nacos.client.NacosInstancesClient;
import com.ch.cloud.nacos.client.NacosSubscribesClient;
import com.ch.cloud.nacos.dto.HistoryDTO;
import com.ch.cloud.nacos.dto.InstanceDTO;
import com.ch.cloud.nacos.dto.SubscriberDTO;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.HistoryPageVO;
import com.ch.cloud.nacos.vo.InstancesPageVO;
import com.ch.cloud.nacos.vo.SubscribesPageVO;
import com.ch.cloud.types.NamespaceType;
import com.ch.cloud.upms.client.UpmsProjectClientService;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.cloud.utils.ContextUtil;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.pojo.VueRecord2;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.s.ApproveStatus;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    private INamespaceService            namespaceService;
    @Autowired
    private NacosNamespaceValidator      nacosNamespaceValidator;
    @Autowired
    private IUserNamespaceService        userNamespaceService;
    @Autowired
    private INamespaceApplyRecordService namespaceApplyRecordService;

    @Autowired
    private NacosHistoryClient    nacosHistoryClient;
    @Autowired
    private NacosInstancesClient  nacosInstancesClient;
    @Autowired
    private NacosSubscribesClient nacosSubscribesClient;

    @Autowired
    private UpmsProjectClientService upmsProjectClientService;


    @ApiOperation(value = "查询空间列表", notes = "查询用户命名空间")
    @GetMapping(value = {"{projectId:[0-9]+}/namespaces"})
    public Result<VueRecord2> namespaces(@PathVariable Long projectId) {
        return ResultUtils.wrap(() -> {
            List<NamespaceDto> records = userNamespaceService.findNamespacesByUsernameAndProjectId(ContextUtil.getUser(), projectId, NamespaceType.NACOS);
            return records.stream().map(e -> {
                VueRecord2 record = new VueRecord2();
                record.setValue(e.getId().toString());
                record.setLabel(e.getName());
                record.setKey(e.getUid());
                return record;
            }).collect(Collectors.toList());
        });
    }

    @ApiOperation(value = "分页查询", notes = "分页查询用户项目配置")
    @GetMapping(value = {"{projectId:[0-9]+}/history"})
    public PageResult<HistoryDTO> history(@PathVariable Long projectId, HistoryPageVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<HistoryPageVO> entity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            record.setTenant(record.getNamespaceId());
            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
            record.setGroup(result.get().getCode());
            return nacosHistoryClient.fetchPage(entity);
        });
    }

    @ApiOperation(value = "分页查询", notes = "分页查询用户项目配置")
    @GetMapping(value = {"{projectId:[0-9]+}/instances"})
    public PageResult<InstanceDTO> instances(@PathVariable Long projectId, InstancesPageVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<InstancesPageVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
            record.setServiceName(result.get().getCode());
            return nacosInstancesClient.fetchPage(clientEntity);
        });
    }

    @ApiOperation(value = "分页查询", notes = "分页查询用户项目配置")
    @GetMapping(value = {"{projectId:[0-9]+}/subscribers"})
    public PageResult<SubscriberDTO> subscribers(@PathVariable Long projectId, SubscribesPageVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<SubscribesPageVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
            record.setServiceName(result.get().getCode());
            return nacosSubscribesClient.fetchPage(clientEntity);
        });
    }


    @PostMapping({"apply/{projectId:[0-9]+}/namespaces"})
    public Result<Boolean> apply(@PathVariable Long projectId, @RequestBody List<Long> namespaceIds) {
        return ResultUtils.wrap(() -> {
            nacosNamespaceValidator.validProjectNamespace(projectId, namespaceIds);
            NamespaceApplyRecord record = new NamespaceApplyRecord();
            record.setCreateBy(ContextUtil.getUser());
            record.setType(NamespaceType.NACOS.getCode());
            record.setDataKey(projectId + "");
            record.setStatus(ApproveStatus.STAY.getCode() + "");
            List<NamespaceApplyRecord> list = namespaceApplyRecordService.find(record);
            if (!list.isEmpty()) {
                ExceptionUtils._throw(PubError.EXISTS, "已提交申请,请联系管理员审核！");
            }
            Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);

            JSONObject object = new JSONObject();
            object.put("userId", ContextUtil.getUser());
            object.put("projectId", projectId);
            object.put("projectName", result.get().getName());
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
