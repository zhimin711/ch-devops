package com.ch.cloud.kafka.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import springfox.documentation.annotations.ApiIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@ApiModel("Kafka 主题配置信息")

public class TopicExtInfo implements Serializable {


    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @ApiModelProperty(value = "ID", hidden = true)
    private Long id;

    /**
     * 集群名称
     */
    @ApiModelProperty("集群名称")
    private String clusterName;

    /**
     * 主题名称
     */
    @ApiModelProperty("主题名称")
    private String topicName;

    /**
     * 存储类型：STRING, JSON, PROTO_STUFF
     */
    @ApiModelProperty("存储类型")
    private String type;

    /**
     * 类文件
     */
    @ApiModelProperty("类文件")
    private String classFile;

    /**
     * 类名称
     */
    @ApiModelProperty("类名称")
    private String className;

    /**
     * 描述
     */
    @ApiModelProperty("描述")
    private String description;

    /**
     * 状态：0. 1. 2.
     */
    private String status;

    /**
     * 创建时间
     */
    @ApiModelProperty(hidden = true)
    private Date createAt;

    /**
     * 创建人
     */
    @ApiModelProperty(hidden = true)
    private String createBy;

    /**
     * 更新时间
     */
    @ApiModelProperty(hidden = true)
    private Date updateAt;

    /**
     * 更新人
     */
    @ApiModelProperty(hidden = true)
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

}