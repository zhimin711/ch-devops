package com.ch.cloud.kafka.admin;

/**
 * @author 01370603
 * @date 2018/9/19 17:49
 */
public class KafkaConsumerConfig {

    private String zookeeperConn;

    public String getZookeeperConn() {
        return zookeeperConn;
    }

    public void setZookeeperConn(String zookeeperConn) {
        this.zookeeperConn = zookeeperConn;
    }
}
