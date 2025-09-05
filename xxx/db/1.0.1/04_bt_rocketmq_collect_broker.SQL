DROP TABLE IF EXISTS `bt_rocketmq_collect_topic`;

-- 创建 broker_collect 表
CREATE TABLE IF NOT EXISTS `rmq_broker_collect` (
                                                `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                                `name_srv_addr` VARCHAR(255) NOT NULL,
    `broker` VARCHAR(255) NOT NULL,
    `collect_date` DATE NOT null,
    `collect_time` DATETIME NOT NULL,
    `average_tps` DECIMAL(18,5) NOT NULL,
    INDEX idx_broker_name (broker),
    INDEX idx_collect_date (collect_date)
    );
