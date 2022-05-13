package com.ch.cloud.kafka.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "bt_content_search")
public class BtContentSearch {
    /**
     * 主键
     */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 集群
     */
    @Column(name = "CLUSTER")
    private String cluster;

    /**
     * 主题
     */
    @Column(name = "TOPIC")
    private String topic;

    /**
     * 状态：0.全量 1.最新 2.最早
     */
    @Column(name = "TYPE")
    private String type;

    /**
     * 搜索量
     */
    @Column(name = "SIZE")
    private Integer size;

    /**
     * 描述
     */
    @Column(name = "CONTENT")
    private String content;

    /**
     * 状态：0.待搜索 1.开始 2.完成 3. 4.超时 5.中断（结果太多）
     */
    @Column(name = "STATUS")
    private String status;

    /**
     * 创建时间
     */
    @Column(name = "CREATE_AT")
    private Date createAt;

    /**
     * 创建人
     */
    @Column(name = "CREATE_BY")
    private String createBy;

    /**
     * 更新时间
     */
    @Column(name = "UPDATE_AT")
    private Date updateAt;

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
     * 获取集群
     *
     * @return CLUSTER - 集群
     */
    public String getCluster() {
        return cluster;
    }

    /**
     * 设置集群
     *
     * @param cluster 集群
     */
    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    /**
     * 获取主题
     *
     * @return TOPIC - 主题
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 设置主题
     *
     * @param topic 主题
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * 获取状态：0.全量 1.最新 2.最早
     *
     * @return TYPE - 状态：0.全量 1.最新 2.最早
     */
    public String getType() {
        return type;
    }

    /**
     * 设置状态：0.全量 1.最新 2.最早
     *
     * @param type 状态：0.全量 1.最新 2.最早
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取搜索量
     *
     * @return SIZE - 搜索量
     */
    public Integer getSize() {
        return size;
    }

    /**
     * 设置搜索量
     *
     * @param size 搜索量
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * 获取描述
     *
     * @return CONTENT - 描述
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置描述
     *
     * @param content 描述
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取状态：0.待搜索 1.开始 2.完成 3. 4.超时 5.中断（结果太多）
     *
     * @return STATUS - 状态：0.待搜索 1.开始 2.完成 3. 4.超时 5.中断（结果太多）
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态：0.待搜索 1.开始 2.完成 3. 4.超时 5.中断（结果太多）
     *
     * @param status 状态：0.待搜索 1.开始 2.完成 3. 4.超时 5.中断（结果太多）
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
}