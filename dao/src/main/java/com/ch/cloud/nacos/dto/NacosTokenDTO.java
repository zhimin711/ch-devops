package com.ch.cloud.nacos.dto;

import lombok.Data;

/**
 * 描述： NacosTokenDTO
 *
 * @author Zhimin.Ma
 * @since 2022/6/29 10:08
 */
@Data
public class NacosTokenDTO {

    private String accessToken;

    private Boolean globalAdmin;
    /**
     * 过期时间（秒）
     */
    private Long tokenTtl;

    private String username;
}
