package com.ch.cloud.kafka.model;

import javax.persistence.*;

@Table(name = "bt_content_record")
public class BtContentRecord {
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
    private Long messageOffset;

    /**
     * 描述
     */
    @Column(name = "CONTENT")
    private String content;

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
     * 获取搜索记录主键
     *
     * @return SID - 搜索记录主键
     */
    public Long getSid() {
        return sid;
    }

    /**
     * 设置搜索记录主键
     *
     * @param sid 搜索记录主键
     */
    public void setSid(Long sid) {
        this.sid = sid;
    }

    /**
     * 获取分区ID
     *
     * @return PARTITION_ID - 分区ID
     */
    public Integer getPartitionId() {
        return partitionId;
    }

    /**
     * 设置分区ID
     *
     * @param partitionId 分区ID
     */
    public void setPartitionId(Integer partitionId) {
        this.partitionId = partitionId;
    }

    /**
     * 获取分区索引
     *
     * @return MESSAGE_OFFSET - 分区索引
     */
    public Long getMessageOffset() {
        return messageOffset;
    }

    /**
     * 设置分区索引
     *
     * @param messageOffset 分区索引
     */
    public void setMessageOffset(Long messageOffset) {
        this.messageOffset = messageOffset;
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
}