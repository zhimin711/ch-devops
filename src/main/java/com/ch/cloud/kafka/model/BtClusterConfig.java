package com.ch.cloud.kafka.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Table(name = "bt_cluster")
@Data
@ToString
public class BtClusterConfig implements Serializable {
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
    @Column(name = "CLUSTER_NAME")
    private String clusterName;

    /**
     * ZOOKEEPER
     */
    @Column(name = "ZOOKEEPER")
    private String zookeeper;

    /**
     * BROKERS
     */
    @Column(name = "BROKERS")
    private String brokers;

    @Column(length = 200)
    private String securityProtocol;
    @Column(length = 200)
    private String saslMechanism;
    @Column(length = 200)
    private String authUsername;
    @Column(length = 200)
    private String authPassword;

    /**
     * 描述
     */
    @Column(name = "DESCRIPTION")
    private String description;

    /**
     * 状态：0. 1. 2.
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
    private Integer topicCount;
    @Transient
    private Integer brokerCount;
    @Transient
    private Integer consumerCount;

}