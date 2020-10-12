package com.ch.cloud.kafka.pojo;

import lombok.Data;

import java.util.List;

/**
 * decs:
 *
 * @author 01370603
 * @date 2020/1/6
 */
@Data
public class DubboCall2 {

    private String interfaceName;
    private String method;
    private String[] paramJson;
    private String paramClassName;
    private String address;
    private String version;


}

