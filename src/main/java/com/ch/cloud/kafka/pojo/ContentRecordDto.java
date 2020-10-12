package com.ch.cloud.kafka.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * ContentRecordDto 扩展对象
 * 
 * @author zhimin.ma
 * @date Wed Oct 30 17:36:47 CST 2019
 */
@Data
public class ContentRecordDto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 搜索记录主键
     */
    private Long sid;

    /**
     * 分区ID
     */
    private Integer partitionId;

    /**
     * 分区索引
     */
    private Long messageOffset;

    /**
     * 描述
     */
    private String content;

}