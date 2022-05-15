package com.ch.cloud.redis.entity;

/**
 * @author Jay.H.Zou
 * @since 9/25/2019
 */
public class DataType {

    private DataType() {
    }

    public static final Integer NODE = 0;

    /**
     * monitor default: calculate node info
     */
    public static final Integer AVG = 1;

    public static final Integer MAX = 2;

    public static final Integer MIN = -1;
}
