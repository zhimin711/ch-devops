package com.ch.cloud.nacos.dto;

import com.ch.pojo.VueRecord;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/5/16
 */
@Data
public class UserNamespaceDTO {

    private List<VueRecord> clusters = new ArrayList<>();

    public Map<Long, List<VueRecord>> namespacesMap;

    public void putNamespaces(Long clusterId, List<VueRecord> namespaces) {
        if (namespacesMap == null) {
            namespacesMap = new HashMap<>();
        }
        namespacesMap.put(clusterId, namespaces);
    }
}
