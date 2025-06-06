DROP TABLE IF EXISTS `bt_rocketmq_collect_topic`;
CREATE TABLE IF NOT EXISTS `rmq_topic_collect` (
                                                   `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                   `name_srv_addr` VARCHAR(255) NOT NULL,
    `topic` VARCHAR(255) NOT NULL,
    `collect_date` DATE NOT null,
    `collect_time` DATETIME NOT NULL,
    `in_tps` DECIMAL(18,5) NOT NULL,
    `in_msg_cnt_today` BIGINT NOT NULL,
    `out_tps` DECIMAL(18,5) NOT NULL,
    `out_msg_cnt_today` BIGINT NOT NULL,
    INDEX idx_topic_name (topic),
    INDEX idx_collect_date (collect_date)
    );

