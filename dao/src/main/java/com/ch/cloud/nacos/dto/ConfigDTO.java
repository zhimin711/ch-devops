package com.ch.cloud.nacos.dto;

import lombok.Data;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/5/8 16:58
 */
@Data
public class ConfigDTO {

    private String appName;
    private String content;
    private String dataId;
    private String group;
    private String id;
    private String md5;
    private String tenant;
    private String type;
}
