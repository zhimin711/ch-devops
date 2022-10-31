package com.ch.cloud.devops.vo;

import com.ch.cloud.types.NamespaceType;
import lombok.Data;

/**
 * <p>
 * desc: NamespaceVO
 * </p>
 *
 * @author Zhimin.Ma
 * @since 2022/10/31
 */
@Data
public class NamespaceVO {
    
    private Long   clusterId;
    
    private NamespaceType type;
}
