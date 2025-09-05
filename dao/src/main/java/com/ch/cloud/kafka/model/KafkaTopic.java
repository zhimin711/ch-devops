package com.ch.cloud.kafka.model;

import lombok.Data;

import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.Size;

@Data
@Table(name = "bt_kafka_topic")
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
    @Column(name = "CLUSTER_ID")
    private Long clusterId;

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
    @Size(min = 1)
    private Integer partitionSize;

    /**
     * 复制数（备份）
     */
    @Column(name = "REPLICA_SIZE")
    @Size(min = 1)
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

}