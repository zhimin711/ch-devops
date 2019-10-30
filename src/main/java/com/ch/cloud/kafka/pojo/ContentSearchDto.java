package com.ch.cloud.kafka.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ContentSearchDto 扩展对象
 * 
 * @author 01370603
 * @date Wed Oct 30 17:38:37 CST 2019
 */
@Data
public class ContentSearchDto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 集群
     */
    private String cluster;

    /**
     * 主题
     */
    private String topic;

    /**
     * 状态：0.全量 1.最新 2.最早
     */
    private String type;

    /**
     * 搜索量
     */
    private Integer size;

    /**
     * 描述
     */
    private String content;

    /**
     * 状态：0.待搜索 1.开始 2.完成 3. 4.超时 5.中断（结果太多）
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

}