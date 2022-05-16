package com.ch.cloud.nacos.controller;

import com.ch.cloud.devops.dto.NamespaceDto;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.nacos.client.NacosServicesClient;
import com.ch.cloud.nacos.domain.NacosCluster;
import com.ch.cloud.nacos.dto.ServiceDTO;
import com.ch.cloud.nacos.dto.UserNamespaceDTO;
import com.ch.cloud.nacos.service.INacosClusterService;
import com.ch.cloud.nacos.service.INacosNamespaceProjectService;
import com.ch.cloud.nacos.validators.NacosNamespaceValidator;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.ServicesPageVO;
import com.ch.cloud.types.NamespaceType;
import com.ch.cloud.utils.ContextUtil;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.VueRecordUtils;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@RestController
@RequestMapping("/nacos/projects/configs")
public class NacosProjectConfigsController {

    @Autowired
    private NacosNamespaceValidator nacosNamespaceValidator;
    @Autowired
    private NacosServicesClient nacosServicesClient;
    @Autowired
    private IUserNamespaceService userNamespaceService;
    @Autowired
    private INacosClusterService nacosClusterService;


    @ApiOperation(value = "分页查询", notes = "分页查询命名空间")
    @GetMapping(value = {"namespaces"})
    public Result<UserNamespaceDTO> namespaces() {
        return ResultUtils.wrap(() -> {
            UserNamespaceDTO dto = new UserNamespaceDTO();
            List<NamespaceDto> namespaces = userNamespaceService.findNamespacesByUsernameAndProjectId(ContextUtil.getUser(), ContextUtil.getTenant(), NamespaceType.NACOS);
            if(CommonUtils.isEmpty(namespaces)) return dto;
            Set<Long> ids = namespaces.stream().map(NamespaceDto::getClusterId).collect(Collectors.toSet());
            List<NacosCluster> clusters = nacosClusterService.findByPrimaryKeys(ids);
            dto.setClusters(VueRecordUtils.covertIdList(clusters));
            dto.setNamespaces(VueRecordUtils.covertIdList(namespaces));
            return dto;
        });
    }

}
