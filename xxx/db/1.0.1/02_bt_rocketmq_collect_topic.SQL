drop table if exists rmq_topic_collect;
CREATE TABLE rmq_topic_collect (
                                   id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
                                   name_srv_addr VARCHAR(255) NOT NULL,
                                   topic VARCHAR(255) NOT NULL,
                                   collect_time DATETIME NOT NULL,
                                   in_tps DECIMAL(18,5) NOT NULL,
                                   in_msg_cnt_today BIGINT NOT NULL,
                                   out_tps DECIMAL(18,5) NOT NULL,
                                   out_msg_cnt_today BIGINT NOT NULL,
                                   collect_date DATE NOT NULL,
                                   PRIMARY KEY (id, collect_date),
                                   KEY idx_topic_name (topic),
                                   KEY idx_collect_date (collect_date)
)
    PARTITION BY RANGE (TO_DAYS(collect_date)) (
  PARTITION p_initial VALUES LESS THAN (MAXVALUE)
);

-- 创建存储过程
DROP PROCEDURE IF EXISTS ch_devops.manage_rmq_partitions;

DELIMITER $$
$$
CREATE DEFINER=`root`@`%` PROCEDURE `ch_devops`.`manage_rmq_partitions`()
BEGIN
    DECLARE db_name VARCHAR(64);
    DECLARE p_date DATE;
    DECLARE p_name VARCHAR(20);
    DECLARE p_value INT;
    DECLARE p_exists INT;
    DECLARE i INT;
    DECLARE keep_partition_count INT DEFAULT 0;
    DECLARE error_msg VARCHAR(255);

    -- 获取当前数据库名
SELECT DATABASE() INTO db_name;

-- 检查并创建未来7天的分区
SET i = 0;
    WHILE i < 7 DO
        SET p_date = DATE_ADD(CURDATE(), INTERVAL i DAY);
        SET p_name = CONCAT('p_', DATE_FORMAT(p_date, '%Y%m%d'));
        SET p_value = TO_DAYS(p_date);

        -- 检查分区是否已存在（显式指定排序规则）
SELECT COUNT(*) INTO p_exists
FROM information_schema.PARTITIONS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'rmq_topic_collect'
  AND PARTITION_NAME = CONCAT('p_', DATE_FORMAT(p_date, '%Y%m%d'))
;

-- 如果分区不存在则创建
IF p_exists = 0 THEN
BEGIN
                DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
BEGIN
GET DIAGNOSTICS CONDITION 1 error_msg = MESSAGE_TEXT;
SELECT CONCAT('Error creating partition ', p_name, ': ', error_msg) AS message;
END;

                SET @sql = CONCAT('ALTER TABLE rmq_topic_collect REORGANIZE PARTITION p_initial INTO (
                    PARTITION ', p_name, ' VALUES LESS THAN (', p_value, '),
                    PARTITION p_initial VALUES LESS THAN (MAXVALUE)
                )');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT CONCAT('Created partition: ', p_name) AS message;
END;
ELSE
SELECT CONCAT('Partition already exists: ', p_name) AS message;
END IF;

        SET i = i + 1;
END WHILE;

    -- 计算需要保留的分区数量（确保至少保留一个分区）
SELECT COUNT(*) INTO keep_partition_count
FROM information_schema.PARTITIONS
WHERE TABLE_SCHEMA = db_name
  AND TABLE_NAME = 'rmq_topic_collect'
  AND PARTITION_NAME != 'p_initial'
    AND PARTITION_DESCRIPTION >= TO_DAYS(DATE_SUB(CURDATE(), INTERVAL 6 DAY));

-- 只有当我们有足够的新分区时才删除旧分区
IF keep_partition_count > 0 THEN
        -- 删除超过7天的旧分区
        SET @min_date_to_keep = DATE_SUB(CURDATE(), INTERVAL 6 DAY); -- 保留7天（今天+过去6天）

        -- 构建要删除的分区列表（显式指定排序规则）
SELECT GROUP_CONCAT(PARTITION_NAME) INTO @partitions_to_drop
FROM information_schema.PARTITIONS
WHERE TABLE_SCHEMA = db_name
  AND TABLE_NAME = 'rmq_topic_collect'
  AND PARTITION_NAME != 'p_initial'
        AND PARTITION_DESCRIPTION < TO_DAYS(@min_date_to_keep);

-- 如果有需要删除的分区则执行删除
IF @partitions_to_drop IS NOT NULL AND @partitions_to_drop != '' THEN
BEGIN
                DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
BEGIN
GET DIAGNOSTICS CONDITION 1 error_msg = MESSAGE_TEXT;
SELECT CONCAT('Error dropping partitions: ', error_msg) AS message;
END;

                SET @sql = CONCAT('ALTER TABLE rmq_topic_collect DROP PARTITION ', @partitions_to_drop);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT CONCAT('Dropped partitions: ', @partitions_to_drop) AS message;
END;
END IF;
END IF;
END$$
DELIMITER ;

-- 创建一个事件
CREATE EVENT event_manage_rmq_partitions
ON SCHEDULE EVERY 1 DAY
STARTS TIMESTAMP(CURRENT_DATE, '01:00:00')
DO
CALL manage_rmq_partitions();