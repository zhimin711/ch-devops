package com.ch.cloud.nacos.validators;

import com.ch.cloud.devops.domain.Namespace;
import com.ch.cloud.devops.dto.ProjectNamespaceDTO;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.nacos.client.NacosUserClient;
import com.ch.cloud.nacos.service.INacosNamespaceProjectService;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.ConfigVO;
import com.ch.cloud.nacos.vo.NamespaceVO;
import com.ch.cloud.upms.client.UpmsProjectClientService;
import com.ch.cloud.upms.client.UpmsUserClientService;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.cloud.upms.dto.ProjectRoleDto;
import com.ch.cloud.utils.ContextUtil;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.utils.AssertUtils;
import com.ch.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/15 11:07
 */
@Component
public class NacosNamespaceValidator {

    @Autowired
    private INamespaceService             namespaceService;
    @Autowired
    private IUserNamespaceService         userNamespaceService;
    @Autowired
    private INacosNamespaceProjectService namespaceProjectService;
    @Autowired
    private UpmsProjectClientService      upmsProjectClientService;
    @Autowired
    private UpmsUserClientService         upmsUserClientService;

    @Autowired
    private INacosNamespaceProjectService nacosNamespaceProjectService;

    @Autowired
    private NacosUserClient nacosUserClient;

    public <T extends NamespaceVO> ClientEntity<T> valid(T record) {
        AssertUtils.isEmpty(record.getNamespaceId(), PubError.NON_NULL, "空间ID");
        Namespace namespace = namespaceService.findWithCluster(record.getNamespaceId());
        AssertUtils.isNull(namespace, PubError.NOT_EXISTS, "集群ID：" + record.getNamespaceId());
        record.setNamespaceId(namespace.getUid());

        ClientEntity<T> clientEntity = ClientEntity.build(namespace.getCluster(), record);
        nacosUserClient.login(clientEntity);
        return clientEntity;
    }

    public <T extends NamespaceVO> ClientEntity<T> validUserNamespace(Long projectId, T record) {
        AssertUtils.isEmpty(record.getNamespaceId(), PubError.NON_NULL, "空间ID");
        AssertUtils.isFalse(userNamespaceService.exists(ContextUtil.getUser(), record.getNamespaceId(), projectId),
                PubError.NOT_AUTH, "项目" + projectId);
        return valid(record);
    }

    public void validProjectNamespace(Long projectId, List<Long> namespaceIds) {
        List<Long> list = namespaceProjectService.findNamespaceIdsByProjectId(projectId);
        namespaceIds.forEach(nid -> AssertUtils.isTrue(!list.contains(nid), PubError.NOT_EXISTS,
                projectId + " project not own namespace id: " + nid));
    }

    public String fetchGroupId(Long projectId, String namespaceId) {
        if (CommonUtils.isDecimal(namespaceId)) {
            ProjectNamespaceDTO dto =
                    nacosNamespaceProjectService.findByProjectIdAndNamespaceId(projectId, Long.valueOf(namespaceId));
            if (dto != null && CommonUtils.isNotEmpty(dto.getGroupId())) {
                return dto.getGroupId();
            }
        }
        Result<ProjectDto> result = upmsProjectClientService.infoByIdOrCode(projectId, null);
        AssertUtils.isNull(result.isEmpty(), PubError.NOT_EXISTS, "项目：" + projectId);
        return result.get().getCode();
    }

    public ClientEntity<ConfigVO> validUserNamespace2edit(Long projectId, ConfigVO record) {
        AssertUtils.isEmpty(record.getNamespaceId(), PubError.NON_NULL, "空间ID");
        Namespace namespace = namespaceService.findWithCluster(record.getNamespaceId());
        AssertUtils.isNull(namespace, PubError.NOT_EXISTS, "空间ID：" + record.getNamespaceId());
        if (CommonUtils.isNotEmpty(namespace.getRoles())) {
            List<ProjectRoleDto> result = upmsUserClientService.findProjectRoles(ContextUtil.getUser(), projectId);
            AssertUtils.isEmpty(result, PubError.NOT_ALLOWED, "空间数据", "编辑");
        }

        record.setNamespaceId(namespace.getUid());
        ClientEntity<ConfigVO> clientEntity = ClientEntity.build(namespace.getCluster(), record);
        nacosUserClient.login(clientEntity);
        return clientEntity;
    }
}
