package com.ch.cloud.kafka.model;

import java.io.Serializable;

/**
 * TopicExtPropDto 扩展对象
 * 
 * @author 01370603
 * @date Tue Oct 13 17:39:34 CST 2020
 */
public class TopicExtPropDto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 所属ID
     */
    private Long pid;

    /**
     * 属性代码
     */
    private String code;

    /**
     * 属性名称
     */
    private String name;

    /**
     * 属性类型
     */
    private String type;

    /**
     * 序号
     */
    private Integer sort;

    /**
     * 值或正则（多个","拼接）
     */
    private String valRegex;

    /**
     * 调整值
     */
    private String valEdit;

    /**
     * 删除值
     */
    private String valDel;

    /**
     * 状态：0.禁用 1.启用 3.删除
     */
    private String status;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Long getPid() {
        return this.pid;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getSort() {
        return this.sort;
    }

    public void setValRegex(String valRegex) {
        this.valRegex = valRegex;
    }

    public String getValRegex() {
        return this.valRegex;
    }

    public void setValEdit(String valEdit) {
        this.valEdit = valEdit;
    }

    public String getValEdit() {
        return this.valEdit;
    }

    public void setValDel(String valDel) {
        this.valDel = valDel;
    }

    public String getValDel() {
        return this.valDel;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}