package com.ch.cloud.kafka.pojo;

import lombok.Data;

/**
 * decs:主题分区信息
 *
 * @author 01370603
 * @date 2019/10/29
 */
@Data
public class PartitionInfo {

    /**
     * 当前分区id
     */
    private int id;
    /**
     * 当前分区IP
     */
    private String host;

    /**
     * 当前分区端口
     */
    private int port;

    /**
     * 当前分区索引开始下标
     */
    private long begin;
    /**
     * 当前分区索引结束下标
     */
    private long end;

    /**
     * 当前分区索引总数
     */
    private long total;
}
