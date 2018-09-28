package com.ch.cloud.kafka.pojo;

import java.io.Serializable;
import java.util.Date;

public class ClusterConfigInfo implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * ZOOKEEPER
     */
    private String zookeeper;

    /**
     * BROKERS
     */
    private String brokers;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0. 1. 2.
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

    private static final long serialVersionUID = 1L;

    /**
     * 获取主键
     *
     * @return ID - 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键
     *
     * @param id 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取集群名称
     *
     * @return CLUSTER_NAME - 集群名称
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * 设置集群名称
     *
     * @param clusterName 集群名称
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    /**
     * 获取ZOOKEEPER
     *
     * @return ZOOKEEPER - ZOOKEEPER
     */
    public String getZookeeper() {
        return zookeeper;
    }

    /**
     * 设置ZOOKEEPER
     *
     * @param zookeeper ZOOKEEPER
     */
    public void setZookeeper(String zookeeper) {
        this.zookeeper = zookeeper;
    }

    /**
     * 获取BROKERS
     *
     * @return BROKERS - BROKERS
     */
    public String getBrokers() {
        return brokers;
    }

    /**
     * 设置BROKERS
     *
     * @param brokers BROKERS
     */
    public void setBrokers(String brokers) {
        this.brokers = brokers;
    }

    /**
     * 获取描述
     *
     * @return DESCRIPTION - 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置描述
     *
     * @param description 描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取状态：0. 1. 2.
     *
     * @return STATUS - 状态：0. 1. 2.
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态：0. 1. 2.
     *
     * @param status 状态：0. 1. 2.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取创建时间
     *
     * @return CREATE_AT - 创建时间
     */
    public Date getCreateAt() {
        return createAt;
    }

    /**
     * 设置创建时间
     *
     * @param createAt 创建时间
     */
    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    /**
     * 获取创建人
     *
     * @return CREATE_BY - 创建人
     */
    public String getCreateBy() {
        return createBy;
    }

    /**
     * 设置创建人
     *
     * @param createBy 创建人
     */
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    /**
     * 获取更新时间
     *
     * @return UPDATE_AT - 更新时间
     */
    public Date getUpdateAt() {
        return updateAt;
    }

    /**
     * 设置更新时间
     *
     * @param updateAt 更新时间
     */
    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    /**
     * 获取更新人
     *
     * @return UPDATE_BY - 更新人
     */
    public String getUpdateBy() {
        return updateBy;
    }

    /**
     * 设置更新人
     *
     * @param updateBy 更新人
     */
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", clusterName=").append(clusterName);
        sb.append(", zookeeper=").append(zookeeper);
        sb.append(", brokers=").append(brokers);
        sb.append(", description=").append(description);
        sb.append(", status=").append(status);
        sb.append(", createAt=").append(createAt);
        sb.append(", createBy=").append(createBy);
        sb.append(", updateAt=").append(updateAt);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}