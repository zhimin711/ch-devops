package com.ch.cloud.kafka.pojo;

import java.util.Properties;

/**
 * @author 01370603
 * @date 2018/9/19 17:00
 */
public class TopicConfig {

    private String zookeeper;
    private String topicName;
    private int partitions = 0;
    private int replicationFactor = 0;
    private Properties properties = new Properties();

    public String getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(String zookeeper) {
        this.zookeeper = zookeeper;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
