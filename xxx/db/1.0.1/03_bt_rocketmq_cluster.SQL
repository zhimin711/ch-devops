CREATE TABLE `bt_rocketmq_cluster`
(
    `ID`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `URL`         varchar(64) NOT NULL COMMENT '集群API地址',
    `NAME`        varchar(150)         DEFAULT NULL COMMENT '集群名称',
    `DESCRIPTION` varchar(255)         DEFAULT NULL COMMENT '描述',
    `DELETED`     tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0.否 1.是',
    `CREATE_AT`   timestamp   NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
    `CREATE_BY`   varchar(64)          DEFAULT NULL COMMENT '创建人',
    `UPDATE_AT`   datetime             DEFAULT NULL COMMENT '更新时间',
    `UPDATE_BY`   varchar(64)          DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_BT_NC_URL` (`URL`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='rocketmq集群表';