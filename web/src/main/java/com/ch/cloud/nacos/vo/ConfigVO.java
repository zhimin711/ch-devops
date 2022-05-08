package com.ch.cloud.nacos.vo;

import lombok.Data;

import java.util.Date;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
@Data
public class ConfigVO {

    private String namespaceId;

    private String dataId;

    private String group;

    private String content;

    private String appName;

    private String desc;
    private String type;
    private String id;

    private String md5;
    private String tenant;
    private Date   createTime;

    private Date   modifyTime;
    private String createUser;
    private String createIp;
    private String use;
    private String effect;
    private String schema;

    private String configTags;
}
