package com.ch.cloud.types;

import com.ch.utils.CommonUtils;

/**
 * desc:命名空间类型
 *
 * @author zhimin
 * @since 2022/4/26 00:11
 */
public enum NamespaceType {

    NACOS(1), ROCKET_MQ(2), KAFKA(3);

    private final int code;

    NamespaceType(int code) {
        this.code = code;
    }

    public static NamespaceType fromCode(Integer code) {
        if (code == null) return NamespaceType.NACOS;
        for (NamespaceType type : NamespaceType.values()) {
            if (type.code == code) return type;
        }
        return NamespaceType.NACOS;
    }

    public static NamespaceType fromCode(String code) {
        if (!CommonUtils.isDecimal(code)) return NamespaceType.NACOS;
        return fromCode(Integer.parseInt(code));
    }

    public int getCode() {
        return code;
    }
}
