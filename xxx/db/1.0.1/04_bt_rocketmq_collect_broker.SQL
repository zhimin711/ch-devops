DROP TABLE IF EXISTS `bt_rocketmq_collect_topic`;
CREATE TABLE `bt_rocketmq_collect_topic`
(
    `id`             BIGINT(20)     NOT NULL AUTO_INCREMENT COMMENT '主键',
    `cluster_id`     bigint(20)     NOT NULL DEFAULT 0 COMMENT '集群ID',
    `topic`          VARCHAR(50)    NULL COMMENT '主题',
    `inTps`          decimal(18, 5)          DEFAULT NULL COMMENT '生产TPS',
    `inMsgCntToday`  bigint(20)     NOT NULL DEFAULT '0' COMMENT '今日生产数',
    `outTps`         decimal(18, 5) NULL COMMENT '消费TPS',
    `outMsgCntToday` bigint(20)     NULL COMMENT '今日消费数',
    `timestamp`      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`ID`),
    KEY `IDX_BT_RCT_cid_topic` (`cluster_id`, `topic`)
) ENGINE = INNODB
  DEFAULT CHARSET = UTF8 COMMENT '模板表';
