package com.ch.cloud.kafka.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "bt_topic")
public class KafkaTopic {
    /**
     * 主键
     */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 集群名称
     */
    @Column(name = "CLUSTER_NAME")
    private String clusterName;

    /**
     * 主题名称
     */
    @Column(name = "TOPIC_NAME")
    private String topicName;

    /**
     * 存储类型：STRING, JSON, PROTO_STUFF
     */
    @Column(name = "TYPE")
    private String type;

    /**
     * 类文件
     */
    @Column(name = "CLASS_FILE")
    private String classFile;

    /**
     * 类名称
     */
    @Column(name = "CLASS_NAME")
    private String className;

    /**
     * 分区数
     */
    @Column(name = "PARTITION_SIZE")
    private Integer partitionSize;

    /**
     * 复制数（备份）
     */
    @Column(name = "REPLICA_SIZE")
    private Integer replicaSize;

    /**
     * 描述
     */
    @Column(name = "DESCRIPTION")
    private String description;

    /**
     * 状态：0.禁用 1.启用 2. 3.删除
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
     * 更新人
     */
    @Column(name = "UPDATE_BY")
    private String updateBy;

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
     * 获取主题名称
     *
     * @return TOPIC_NAME - 主题名称
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * 设置主题名称
     *
     * @param topicName 主题名称
     */
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    /**
     * 获取存储类型：STRING, JSON, PROTO_STUFF
     *
     * @return TYPE - 存储类型：STRING, JSON, PROTO_STUFF
     */
    public String getType() {
        return type;
    }

    /**
     * 设置存储类型：STRING, JSON, PROTO_STUFF
     *
     * @param type 存储类型：STRING, JSON, PROTO_STUFF
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取类文件
     *
     * @return CLASS_FILE - 类文件
     */
    public String getClassFile() {
        return classFile;
    }

    /**
     * 设置类文件
     *
     * @param classFile 类文件
     */
    public void setClassFile(String classFile) {
        this.classFile = classFile;
    }

    /**
     * 获取类名称
     *
     * @return CLASS_NAME - 类名称
     */
    public String getClassName() {
        return className;
    }

    /**
     * 设置类名称
     *
     * @param className 类名称
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * 获取分区数
     *
     * @return PARTITION_SIZE - 分区数
     */
    public Integer getPartitionSize() {
        return partitionSize;
    }

    /**
     * 设置分区数
     *
     * @param partitionSize 分区数
     */
    public void setPartitionSize(Integer partitionSize) {
        this.partitionSize = partitionSize;
    }

    /**
     * 获取复制数（备份）
     *
     * @return REPLICA_SIZE - 复制数（备份）
     */
    public Integer getReplicaSize() {
        return replicaSize;
    }

    /**
     * 设置复制数（备份）
     *
     * @param replicaSize 复制数（备份）
     */
    public void setReplicaSize(Integer replicaSize) {
        this.replicaSize = replicaSize;
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
     * 获取状态：0.禁用 1.启用 2. 3.删除
     *
     * @return STATUS - 状态：0.禁用 1.启用 2. 3.删除
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态：0.禁用 1.启用 2. 3.删除
     *
     * @param status 状态：0.禁用 1.启用 2. 3.删除
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
}