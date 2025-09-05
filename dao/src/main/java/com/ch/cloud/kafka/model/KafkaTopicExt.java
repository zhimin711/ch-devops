package com.ch.cloud.kafka.model;

import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

@Data
@Table(name = "bt_kafka_topic_ext")
public class KafkaTopicExt {
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
     * 主题内容类型：BASE, BASE_ARRAY, OBJECT, OBJECT_ARRAY
     */
    @Column(name = "CONTENT_TYPE")
    private String contentType;

    /**
     * Mock线程数
     */
    @Column(name = "THREAD_SIZE")
    private Integer threadSize;

    /**
     * 单线程Mock数据量
     */
    @Column(name = "BATCH_SIZE")
    private Integer batchSize;

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

    @Transient
    private List<KafkaTopicExtProp> props;
    @Transient
    private List<String>            points;
}