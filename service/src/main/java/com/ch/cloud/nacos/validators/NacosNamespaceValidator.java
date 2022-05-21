package com.ch.cloud.nacos.validators;

import com.ch.cloud.devops.domain.Namespace;
import com.ch.cloud.devops.service.INamespaceService;
import com.ch.cloud.devops.service.IUserNamespaceService;
import com.ch.cloud.nacos.vo.ClientEntity;
import com.ch.cloud.nacos.vo.NamespaceVO;
import com.ch.cloud.utils.ContextUtil;
import com.ch.e.PubError;
import com.ch.utils.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public <T extends NamespaceVO> ClientEntity<T> valid(T record) {
        AssertUtils.isEmpty(record.getNamespaceId(), PubError.NON_NULL, "空间ID");
        Namespace namespace = namespaceService.findWithCluster(record.getNamespaceId());
        AssertUtils.isNull(namespace, PubError.NOT_EXISTS, "集群ID：" + record.getNamespaceId());
        record.setNamespaceId(namespace.getUid());
        return new ClientEntity<>(namespace.getAddr(), record);
    }

    public <T extends NamespaceVO> ClientEntity<T> validUserNamespace(Long projectId, T record) {
        AssertUtils.isEmpty(record.getNamespaceId(), PubError.NON_NULL, "空间ID");
        AssertUtils.isFalse(userNamespaceService.exists(ContextUtil.getUser(), record.getNamespaceId(), projectId), PubError.NOT_AUTH, "项目" + projectId);
        Namespace namespace = namespaceService.findWithCluster(record.getNamespaceId());
        AssertUtils.isNull(namespace, PubError.NOT_EXISTS, "集群ID：" + record.getNamespaceId());
        record.setNamespaceId(namespace.getUid());
        return new ClientEntity<>(namespace.getAddr(), record);
    }
}
