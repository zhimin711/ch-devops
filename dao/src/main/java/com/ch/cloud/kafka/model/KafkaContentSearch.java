package com.ch.cloud.kafka.model;

import lombok.Data;

import java.util.Date;
import javax.persistence.*;

@Data
@Table(name = "bt_kafka_content_search")
public class KafkaContentSearch {
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
    @Column(name = "CLUSTER_ID")
    private Long clusterId;

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

}