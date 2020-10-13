package com.ch.cloud.kafka.model;

import javax.persistence.*;
import lombok.Data;

@Data
@Table(name = "bt_topic_ext_prop")
public class BtTopicExtProp {
    /**
     * 主键
     */
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属ID
     */
    @Column(name = "PID")
    private Long pid;

    /**
     * 属性代码
     */
    @Column(name = "CODE")
    private String code;

    /**
     * 属性名称
     */
    @Column(name = "NAME")
    private String name;

    /**
     * 属性类型
     */
    @Column(name = "TYPE")
    private String type;

    /**
     * 序号
     */
    @Column(name = "SORT")
    private Integer sort;

    /**
     * 值或正则（多个","拼接）
     */
    @Column(name = "VAL_REGEX")
    private String valRegex;

    /**
     * 调整值
     */
    @Column(name = "VAL_EDIT")
    private String valEdit;

    /**
     * 删除值
     */
    @Column(name = "VAL_DEL")
    private String valDel;

    /**
     * 状态：0.禁用 1.启用 3.删除
     */
    @Column(name = "STATUS")
    private String status;
}