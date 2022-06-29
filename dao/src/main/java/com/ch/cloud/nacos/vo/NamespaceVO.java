package com.ch.cloud.nacos.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/15 11:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NamespaceVO {

    private String namespaceId;

    private String accessToken = "";
}
