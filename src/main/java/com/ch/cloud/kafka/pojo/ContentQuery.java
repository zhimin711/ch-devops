package com.ch.cloud.kafka.pojo;

import java.io.Serializable;

/**
 * @author 01370603
 * @date 2018/9/19 17:00
 */
public class ContentQuery implements Serializable {

    private String cluster;
    private String topic;
    private String type;
    private String content;

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
