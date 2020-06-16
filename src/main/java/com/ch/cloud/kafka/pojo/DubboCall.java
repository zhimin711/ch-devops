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
public class DubboCall {

    private String interfaceName;
    private String method;
    private List<Object> param;
    private String address;
    private String version;


    @Override
    public String toString() {
        return "CallRequest{" +
                "interfaceName='" + interfaceName + '\'' +
                ", method='" + method + '\'' +
                ", param=" + param +
                ", address='" + address + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}

