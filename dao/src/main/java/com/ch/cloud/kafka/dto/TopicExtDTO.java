package com.ch.cloud.kafka.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * TopicExtDto 扩展对象
 * 
 * @author 01370603
 * @since Tue Oct 13 13:43:34 CST 2020
 */
@Data
public class TopicExtDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 主题名称
     */
    private String topicName;

    /**
     * 主题内容类型：BASE, BASE_ARRAY, OBJECT, OBJECT_ARRAY
     */
    private String contentType;

    /**
     * Mock线程数
     */
    private Integer threadSize;

    /**
     * 单线程Mock数据量
     */
    private Integer batchSize;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0.禁用 1.启用 2. 3.删除
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

}