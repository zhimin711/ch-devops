DROP TABLE IF EXISTS `bt_rocketmq_collect_topic`;
CREATE TABLE `bt_rocketmq_collect_topic`
(
    `id`             BIGINT(20)     NOT NULL AUTO_INCREMENT COMMENT '主键',
    `cluster_id`     bigint(20)     NOT NULL DEFAULT 0 COMMENT '集群ID',
    `broker`          VARCHAR(50)    NULL COMMENT '主题',
    `totalTps`          decimal(18, 5)          DEFAULT NULL COMMENT '总TPS',
    `timestamp`      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`ID`),
    KEY `IDX_BT_RCT_cid_broker` (`cluster_id`, `broker`)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8 COMMENT '模板表';
