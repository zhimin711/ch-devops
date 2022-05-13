package com.ch.cloud.kafka.enums;

/**
 * @author zhimin.ma
 * @date 2018/9/25 20:13
 */
public enum ContentType {
    STRING, JSON, PROTO_STUFF, UNKNOWN;

    public static ContentType from(String type) {
        if (type == null) {
            return UNKNOWN;
        }
        switch (type) {
            case "STRING":
                return STRING;
            case "JSON":
                return JSON;
            case "PROTO_STUFF":
                return PROTO_STUFF;
        }
        return UNKNOWN;
    }
}
