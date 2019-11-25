package com.ch.cloud.kafka.pojo;

import lombok.Data;

import java.util.Properties;

/**
 * @author 01370603
 * @date 2018/9/19 17:00
 */
@Data
public class TopicInfo {

    private String zookeeper;
    private String name;
    private int partitionSize = 0;
    private int replicaSize = 0;

}
