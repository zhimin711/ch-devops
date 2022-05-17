package com.ch.cloud.nacos.controller.user;

import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.nacos.client.*;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.dto.HistoryDTO;
import com.ch.cloud.nacos.dto.InstanceDTO;
import com.ch.cloud.nacos.dto.SubscriberDTO;
import com.ch.cloud.nacos.dto.UserNamespaceDTO;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.*;
import com.ch.cloud.types.NamespaceType;
import com.ch.cloud.utils.ContextUtil;
import com.ch.pojo.VueRecord;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.VueRecordUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@RestController
@RequestMapping("/nacos/user")
public class NacosUserProjectController {

    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    @Autowired
    private IUserNamespaceService userNamespaceService;
    @Autowired
    private INacosClusterService nacosClusterService;
    @Autowired
    private NacosHistoryClient nacosHistoryClient;
    @Autowired
    private NacosInstancesClient nacosInstancesClient;
    @Autowired
    private NacosSubscribesClient nacosSubscribesClient;


    @ApiOperation(value = "分页查询", notes = "分页查询用户命名空间")
    @GetMapping(value = {"{projectId:[0-9]+}/namespaces"})
    public Result<UserNamespaceDTO> namespaces(@PathVariable Long projectId) {
        return ResultUtils.wrap(() -> {
            UserNamespaceDTO dto = new UserNamespaceDTO();
            List<NamespaceDto> namespaces = userNamespaceService.findNamespacesByUsernameAndProjectId(ContextUtil.getUser(), projectId, NamespaceType.NACOS);
            if (CommonUtils.isEmpty(namespaces)) return dto;
            Set<Long> ids = namespaces.stream().map(NamespaceDto::getClusterId).collect(Collectors.toSet());
            List<NacosCluster> clusters = nacosClusterService.findByPrimaryKeys(ids);
            dto.setClusters(VueRecordUtils.covertIdList(clusters));
//            dto.setNamespaces(VueRecordUtils.covertIdList(namespaces));
            Map<Long, List<NamespaceDto>> map = namespaces.stream().collect(Collectors.groupingBy(NamespaceDto::getClusterId));
            map.forEach((k, v) -> {
                List<VueRecord> nsList = VueRecordUtils.covertIdList(v);
                dto.putNamespaces(k, nsList);
            });
            return dto;
        });
    }

    @ApiOperation(value = "分页查询", notes = "分页查询用户项目配置")
    @GetMapping(value = {"{projectId:[0-9]+}/history"})
    public PageResult<HistoryDTO> history(@PathVariable Long projectId, HistoryPageVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<HistoryPageVO> entity = nacosNamespaceValidator.valid(record);
            record.setTenant(record.getNamespaceId());
            return nacosHistoryClient.fetchPage(entity);
        });
    }

    @ApiOperation(value = "分页查询", notes = "分页查询用户项目配置")
    @GetMapping(value = {"{projectId:[0-9]+}/instances"})
    public PageResult<InstanceDTO> instances(@PathVariable Long projectId, InstancesPageVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<InstancesPageVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            return nacosInstancesClient.fetchPage(clientEntity);
        });
    }

    @ApiOperation(value = "分页查询", notes = "分页查询用户项目配置")
    @GetMapping(value = {"{projectId:[0-9]+}/subscribers"})
    public PageResult<SubscriberDTO> subscribers(@PathVariable Long projectId, SubscribesPageVO record) {
        return ResultUtils.wrapPage(() -> {
            ClientEntity<SubscribesPageVO> clientEntity = nacosNamespaceValidator.validUserNamespace(projectId, record);
            return nacosSubscribesClient.fetchPage(clientEntity);
        });
    }
}
