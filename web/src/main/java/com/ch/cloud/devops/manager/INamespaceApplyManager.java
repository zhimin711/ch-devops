package com.ch.cloud.devops.manager;

import com.ch.cloud.devops.domain.NamespaceApplyRecord;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/30
 */
public interface INamespaceApplyManager {
    
    int approveNacos(NamespaceApplyRecord record);
}
