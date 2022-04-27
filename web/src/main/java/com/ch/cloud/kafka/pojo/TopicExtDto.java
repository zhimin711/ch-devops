package com.ch.cloud.kafka.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * TopicExtDto 扩展对象
 * 
 * @author 01370603
 * @date Tue Oct 13 13:43:34 CST 2020
 */
public class TopicExtDto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 主题名称
     */
    private String topicName;

    /**
     * 主题内容类型：BASE, BASE_ARRAY, OBJECT, OBJECT_ARRAY
     */
    private String contentType;

    /**
     * Mock线程数
     */
    private Integer threadSize;

    /**
     * 单线程Mock数据量
     */
    private Integer batchSize;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0.禁用 1.启用 2. 3.删除
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private Date updateAt;

    /**
     * 更新人
     */
    private String updateBy;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterName() {
        return this.clusterName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return this.topicName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setThreadSize(Integer threadSize) {
        this.threadSize = threadSize;
    }

    public Integer getThreadSize() {
        return this.threadSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getBatchSize() {
        return this.batchSize;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Date getCreateAt() {
        return this.createAt;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getCreateBy() {
        return this.createBy;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    public Date getUpdateAt() {
        return this.updateAt;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public String getUpdateBy() {
        return this.updateBy;
    }
}