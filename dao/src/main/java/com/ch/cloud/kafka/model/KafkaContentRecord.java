package com.ch.cloud.kafka.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "bt_kafka_content_record")
public class KafkaContentRecord {
    /**
     * 主键
     */
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 搜索记录主键
     */
    @Column(name = "SID")
    private Long sid;

    /**
     * 分区ID
     */
    @Column(name = "PARTITION_ID")
    private Integer partitionId;

    /**
     * 分区索引
     */
    @Column(name = "MESSAGE_OFFSET")
    @OrderBy("desc")
    private Long messageOffset;

    /**
     * 描述
     */
    @Column(name = "CONTENT")
    private String content;

}