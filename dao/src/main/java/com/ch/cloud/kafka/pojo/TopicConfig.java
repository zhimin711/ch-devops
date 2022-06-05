package com.ch.cloud.kafka.pojo;

import lombok.Data;

import java.util.Properties;

/**
 * @author zhimin.ma
 * @since 2018/9/19 17:00
 */
@Data
public class TopicConfig {

    private String zookeeper;
    private String topicName;
    private int partitions = 0;
    private int replicationFactor = 0;
    private Properties properties = new Properties();

}
