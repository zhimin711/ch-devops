package com.ch.cloud.nacos.validators;

import com.ch.cloud.devops.domain.Namespace;
import com.ch.cloud.devops.dto.ProjectNamespaceDTO;
import com.ch.cloud.devops.enums.Permission;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.nacos.client.NacosUserClient;
import com.ch.cloud.nacos.service.INacosNamespaceProjectService;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.ConfigClientVO;
import com.ch.cloud.nacos.vo.NamespaceClientVO;
import com.ch.cloud.upms.client.UpmsProjectClient;
import com.ch.cloud.upms.client.UpmsUserClient;
import com.ch.cloud.upms.dto.ProjectDto;
import com.ch.cloud.upms.enums.RoleType;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.toolkit.ContextUtil;
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
    private INamespaceService namespaceService;
    
    @Autowired
    private IUserNamespaceService userNamespaceService;
    
    @Autowired
    private INacosNamespaceProjectService namespaceProjectService;
    
    @Autowired
    private UpmsProjectClient upmsProjectClient;
    
    @Autowired
    private UpmsUserClient upmsUserClient;
    
    @Autowired
    private INacosNamespaceProjectService nacosNamespaceProjectService;
    
    @Autowired
    private NacosUserClient nacosUserClient;
    
    public <T extends NamespaceClientVO> ClientEntity<T> valid(T record) {
        Assert.notEmpty(record.getNamespaceId(), PubError.NON_NULL, "空间ID");
        Namespace namespace = namespaceService.findWithCluster(record.getNamespaceId());
        Assert.notNull(namespace, PubError.NOT_EXISTS, "集群ID：" + record.getNamespaceId());
        record.setNamespaceId(namespace.getUid());
        
        ClientEntity<T> clientEntity = ClientEntity.build(namespace.getCluster(), record);
        nacosUserClient.login(clientEntity);
        return clientEntity;
    }
    
    public <T extends NamespaceClientVO> ClientEntity<T> validUserNamespace(Long projectId, T record) {
        Assert.notEmpty(record.getNamespaceId(), PubError.NON_NULL, "空间ID");
        Assert.isTrue(userNamespaceService.exists(ContextUtil.getUsername(), record.getNamespaceId(), projectId),
                PubError.NOT_AUTH, "项目" + projectId);
        return valid(record);
    }
    
    public <T extends NamespaceClientVO> ClientEntity<T> validUserNamespacePermission(Long projectId, T record,
            Permission permission) {
        Assert.notEmpty(record.getNamespaceId(), PubError.NON_NULL, "空间ID");
        boolean expression = userNamespaceService.existsPermission(ContextUtil.getUsername(), record.getNamespaceId(),
                projectId, permission);
        Assert.isTrue(expression, PubError.NOT_AUTH, "项目" + projectId);
        
        return valid(record);
    }
    
    public void validProjectNamespace(Long projectId, List<Long> namespaceIds) {
        List<Long> list = namespaceProjectService.findNamespaceIdsByProjectId(projectId);
        namespaceIds.forEach(nid -> Assert.isTrue(list.contains(nid), PubError.NOT_EXISTS,
                projectId + " project not own namespace id: " + nid));
    }
    
    public String fetchGroupId(Long projectId, String namespaceId) {
        if (CommonUtils.isDecimal(namespaceId)) {
            ProjectNamespaceDTO dto = nacosNamespaceProjectService.findByProjectIdAndNamespaceId(projectId,
                    Long.valueOf(namespaceId));
            if (dto != null && CommonUtils.isNotEmpty(dto.getGroupId())) {
                return dto.getGroupId();
            }
        }
        Result<ProjectDto> result = upmsProjectClient.infoByIdOrCode(projectId, null);
        Assert.notNull(result.isEmpty(), PubError.NOT_EXISTS, "项目：" + projectId);
        return result.get().getCode();
    }
    
    public ClientEntity<ConfigClientVO> validUserNamespace2edit(Long projectId, ConfigClientVO record) {
        Assert.notEmpty(record.getNamespaceId(), PubError.NON_NULL, "空间ID");
        Namespace namespace = namespaceService.findWithCluster(record.getNamespaceId());
        Assert.notNull(namespace, PubError.NOT_EXISTS, "空间ID：" + record.getNamespaceId());
        if (CommonUtils.isNotEmpty(namespace.getRoles())) {
            List<RoleType> list = upmsUserClient.listProjectRoles(ContextUtil.getUsername(), projectId, null);
            Assert.notEmpty(list, PubError.NOT_ALLOWED, "空间数据", "编辑");
        }
        
        record.setNamespaceId(namespace.getUid());
        ClientEntity<ConfigClientVO> clientEntity = ClientEntity.build(namespace.getCluster(), record);
        nacosUserClient.login(clientEntity);
        return clientEntity;
    }
}
