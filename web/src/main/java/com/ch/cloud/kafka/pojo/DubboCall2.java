package com.ch.cloud.kafka.pojo;

import lombok.Data;

import java.util.List;

/**
 * decs:
 *
 * @author zhimin.ma
 * @date 2020/1/6
 */
@Data
public class DubboCall2 {

    private String address;
    private String interfaceName;
    private String version;
    private String method;
    private String paramClassName;
    private String[] paramJson;


}

